package com.uber.matchingservice.service;

import com.uber.matchingservice.event.RideRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideEventConsumet {

    private final MatchingService matchingService;

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service-group"
    )
    public void consumeRideRequestEvent(RideRequestEvent event){
        try {
            matchingService.matchDriverForRide(event);
        } catch (Exception e){
            //In production send to dead letter queue for retry
            log.error("Error procesing ride request: {} - {}", event.rideId(), e.getMessage());
        }
    }
}
