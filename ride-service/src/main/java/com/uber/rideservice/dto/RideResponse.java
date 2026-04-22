package com.uber.rideservice.dto;

import com.uber.rideservice.model.RideStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RideResponse(
        String id,
        String riderId,
        String driverId,
        Double pickupLatitude,
        Double pickupLongitude,
        String pickupAddress,
        Double dropLatitude,
        Double dropLongitude,
        String dropAddress,
        RideStatus status, //tracks lifecycle
        Double estimatedFare,
        Double actualFare,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
}
