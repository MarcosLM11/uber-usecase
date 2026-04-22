package com.uber.locationservice.service;

import com.uber.locationservice.model.dto.DriverLocationRequest;
import com.uber.locationservice.model.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;

    //Redis key for all drivers location
    private static final String DRIVERS_GEO_KEY = "drivers:locations";

    /**
     * Updates driver location in Redis
     * Called every 3 seconds by drivers phone
     * Maps to Redis GEOADD command
     */
    public void updateDriverLocation(DriverLocationRequest request) {
        log.info("Updating location for driver: {}", request.driverId());

        var driverPoint = new Point(request.longitude(),request.latitude());
        redisTemplate.opsForGeo().add(DRIVERS_GEO_KEY, driverPoint, request.driverId());

        log.info("Location updated for driver: {}", request.driverId());
    }

    /**
     * Find nearby drivers given radius
     * Called by Matching Service on ride request
     * Maps to Redis FEORADIUS command
     */
    public List<NearByDriverResponse> findNearByDrivers(Double latitude, Double longitude, Double radius) {
        log.info("Finding drivers near lat: {}, long: {}, withing: {}km", latitude,longitude,radius);

        var point = new Point(longitude,latitude);
        var distance = new Distance(radius, Metrics.KILOMETERS);
        var circle = new Circle(point,distance);
        var result = redisTemplate.opsForGeo().radius(
                DRIVERS_GEO_KEY,
                circle,
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeCoordinates()
                        .includeDistance()
                        .sortAscending()
                        .limit(10)
        );

        var nearbyDrivers = new ArrayList<NearByDriverResponse>();

        if(result != null) {
            result.getContent().forEach(r -> {
                var location = r.getContent();
                nearbyDrivers.add( new NearByDriverResponse(
                        location.getName(),
                        location.getPoint().getY(),
                        location.getPoint().getX(),
                        r.getDistance().getValue())
                );
            });
        }

        log.info("Found {} drivers nearby", nearbyDrivers.size());
        return nearbyDrivers;
    }

    /**
     * Remove driver when they go offline
     * Maps to Redis ZREM command.
     */
    public void removeDriver(String driverId) {
        log.info("Removing driver: {}", driverId);

        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY,driverId);

        log.info("Driver {} removed", driverId);
    }

}
