package com.uber.rideservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RideRequest(
        @NotBlank(message = "Rider id is required")
        String riderId,
        @NotBlank(message = "Rider pickup latitude is required")
        Double pickupLatitude,
        @NotBlank(message = "Rider pickup longitude is required")
        Double pickupLongitude,
        @NotBlank(message = "Rider pickup address is required")
        String pickupAddress,
        @NotBlank(message = "Rider drop latitude is required")
        Double dropLatitude,
        @NotBlank(message = "Rider drop longitude is required")
        Double dropLongitude,
        @NotBlank(message = "Rider drop address is required")
        String dropAddress
) {
}
