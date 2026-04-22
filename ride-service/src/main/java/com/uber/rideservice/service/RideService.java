package com.uber.rideservice.service;

import com.uber.rideservice.dto.RideRequest;
import com.uber.rideservice.dto.RideResponse;
import com.uber.rideservice.event.RideRequestEvent;
import com.uber.rideservice.model.Ride;
import com.uber.rideservice.model.RideStatus;
import com.uber.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository repository;
    private final KafkaTemplate<String, RideRequestEvent> kafkaTemplate;
    private static final String RIDE_REQUESTED_TOPIC = "ride.requested";
    private static final String EX_MESSAGE = "Ride not Found";

    public RideResponse requestRide(RideRequest request) {
        log.info("New ride requested from rider: {}", request.riderId());

        var ride = Ride.builder()
                .riderId(request.riderId())
                .pickupLatitude(request.pickupLatitude())
                .pickupLongitude(request.pickupLongitude())
                .pickupAddress(request.pickupAddress())
                .dropLatitude(request.dropLatitude())
                .dropLongitude(request.dropLongitude())
                .dropAddress(request.dropAddress())
                .estimatedFare(calculateEstimateFared(request))
                .status(RideStatus.REQUESTED).build();

        var rideSaved = repository.save(ride);

        var event = RideRequestEvent.builder()
                 .rideId(rideSaved.getId())
                 .riderId(rideSaved.getRiderId())
                 .pickupLatitude(rideSaved.getPickupLatitude())
                 .pickupLongitude(rideSaved.getPickupLongitude())
                 .pickupAddress(rideSaved.getPickupAddress())
                 .dropLatitude(rideSaved.getDropLatitude())
                 .dropLongitude(rideSaved.getDropLongitude())
                 .dropAddress(rideSaved.getDropAddress()).build();

        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, event);
        log.info("RideRequestEvent published to kafka for ride: {}", rideSaved.getId());

        rideSaved.setStatus(RideStatus.MATCHING); //Update Status to Matching
        repository.save(rideSaved);
        return mapToResponse(rideSaved);
    }

    public void updateRideWithDriver(String rideId, String driverId){
        var ride = repository.findById(rideId).orElseThrow(() -> new RuntimeException(EX_MESSAGE));
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setDriverId(driverId);
        repository.save(ride);
    }

    public RideResponse startRide(String rideId){
        var ride = repository.findById(rideId).orElseThrow(() -> new RuntimeException(EX_MESSAGE));

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new RuntimeException("Ride cannot be started. Current Status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        repository.save(ride);
        return mapToResponse(ride);
    }

    public RideResponse completeRide(String rideId){
        var ride = repository.findById(rideId).orElseThrow(() -> new RuntimeException(EX_MESSAGE));

        if (ride.getStatus() != RideStatus.RIDE_STARTED) {
            throw new RuntimeException("Ride cannot be completed. Current Status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        repository.save(ride);
        return mapToResponse(ride);
    }

    public RideResponse cancelRide(String rideId){
        var ride = repository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride not Found"));

        ride.setStatus(RideStatus.CANCELLED);
        repository.save(ride);
        return mapToResponse(ride);
    }

    public RideResponse getRideById(String rideId){
        var ride = repository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride not Found"));
        return mapToResponse(ride);
    }

    public List<RideResponse> getRidesByRider(String riderId){
        var rides = repository.findByRiderIdOrderByCreatedAtDesc(riderId);
        return rides.stream().map(this::mapToResponse).toList();
    }

    private Double calculateEstimateFared(RideRequest request){
        //Simple Haversine distance calculation
        var lat1 = Math.toRadians(request.pickupLatitude());
        var lat2 = Math.toRadians(request.dropLatitude());
        var long1 = Math.toRadians(request.pickupLongitude());
        var long2 = Math.toRadians(request.dropLongitude());

        var dlat = lat2 - lat1;
        var dlong = long2 - long1;

        var a = Math.pow(Math.sin(dlat/2),2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlong/2),2);
        var c = 2 * Math.asin(Math.sqrt(a));
        var distanceKM = 6371 * c;

        //Base fare: 50 + 12 per km
        var fare = 50 + (distanceKM * 12);
        return Math.round(fare *  100.0) / 100.0;
    }

    private RideResponse mapToResponse(Ride ride) {
        return RideResponse.builder()
                .id(ride.getId())
                .riderId(ride.getRiderId())
                .driverId(ride.getDriverId())
                .pickupLatitude(ride.getPickupLatitude())
                .pickupLongitude(ride.getPickupLongitude())
                .pickupAddress(ride.getPickupAddress())
                .dropLatitude(ride.getDropLatitude())
                .dropLongitude(ride.getDropLongitude())
                .dropAddress(ride.getDropAddress())
                .status(ride.getStatus())
                .estimatedFare(ride.getEstimatedFare())
                .actualFare(ride.getActualFare())
                .createdAt(ride.getCreatedAt())
                .updatedAt(ride.getUpdatedAt())
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt() )
                .build();
    }

}
