package com.uber.rideservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RideRequest(
        @NotBlank(message = "Rider id is required")
        String riderId,
        Double pickupLatitude,
        Double pickupLongitude,
        @NotBlank(message = "Rider pickup address is required")
        String pickupAddress,
        Double dropLatitude,
        Double dropLongitude,
        @NotBlank(message = "Rider drop address is required")
        String dropAddress
) {
}
