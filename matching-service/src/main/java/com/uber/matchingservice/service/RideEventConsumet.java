package com.uber.matchingservice.service;

import com.uber.matchingservice.event.RideRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideEventConsumet {

    private final MatchingService matchingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service-group"
    )
    public void consumeRideRequestEvent(String payload){
        try {
            RideRequestEvent event = objectMapper.readValue(payload, RideRequestEvent.class);
            matchingService.matchDriverForRide(event);
        } catch (Exception e){
            //In production send to dead letter queue for retry
            log.error("Error procesing ride request: {}", e.getMessage());
        }
    }
}
