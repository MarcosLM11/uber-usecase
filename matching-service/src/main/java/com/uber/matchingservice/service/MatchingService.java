package com.uber.matchingservice.service;

import com.uber.matchingservice.client.LocationServiceClient;
import com.uber.matchingservice.dto.NearByDriverResponse;
import com.uber.matchingservice.event.RideMatchEvent;
import com.uber.matchingservice.event.RideRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchEvent> kafkaTemplate;
    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final Double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    public void matchDriverForRide(RideRequestEvent event) {
        var nearbyDrivers = locationServiceClient.getNearByDrivers(
                event.pickupLatitude(),
                event.pickupLongitude(),
                DEFAULT_SEARCH_RADIUS_KM
        );

        if(nearbyDrivers.isEmpty()) {
            log.warn("No drivers found near ride: {}", event.rideId());
            return;
        }

        var bestDriver = findBestDriver(nearbyDrivers);

        if(bestDriver.isEmpty()) {
            log.warn("Could not found suitable driver for ride");
            return;
        }

        var assignedDriver = bestDriver.get();

        var matchedEvent = RideMatchEvent.builder()
                .rideId(event.rideId())
                .riderId(event.riderId())
                .driverId(assignedDriver.driverId())
                .driverLatitude(assignedDriver.latitude())
                .driverLongitude(assignedDriver.longitude())
                .distanceToPickupKm(assignedDriver.distance())
                .build();

        kafkaTemplate.send(RIDE_MATCHED_TOPIC,matchedEvent);
        log.info("RideMatchedEvent published");
    }

    /**
     * Driver scoring algorithm
     * Distance: 70%
     * Rating: 30%
     * Score = (1/distance) * distanceWeight + rating * ratingWeight
     */
    private Optional<NearByDriverResponse> findBestDriver(List<NearByDriverResponse> drivers){
        var distanceWeight = 0.7;
        var ratingWeight = 0.3;

        return drivers.stream()
                .max(Comparator.comparingDouble(driver -> {
                    //Distance score: closer = higer score, add 0.1 to avoid division by zero
                    var distanceScore = 1.0/(driver.distance()+0.1);
                    //Simulated rating between 4.0 and 5.0. In production fetch from Driver service
                    var simulatedRating = 4.0 + Math.random();
                    //Final weight score
                    return (distanceScore * distanceWeight) + (simulatedRating * ratingWeight);

                }));
    }

}
