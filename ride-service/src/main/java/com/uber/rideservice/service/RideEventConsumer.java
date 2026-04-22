package com.uber.rideservice.service;

import com.uber.rideservice.event.RideMatchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideEventConsumer {

    private final RideService rideService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "ride.matched",
            groupId = "ride-service-group"
    )
    public void consumeRideMatchedEvent(String payload){
        try {
            RideMatchEvent event = objectMapper.readValue(payload, RideMatchEvent.class);
            log.info("RideMatchedEvent received for ride: {}, driver: {}", event.rideId(), event.driverId());
            rideService.updateRideWithDriver(event.rideId(), event.driverId());
        } catch (Exception e) {
            log.error("Error processing ride matched event: {}", e.getMessage());
        }
    }
}
