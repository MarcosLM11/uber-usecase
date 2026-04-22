package com.uber.matchingservice.event;

import lombok.Builder;

@Builder
public record RideRequestEvent(
        String rideId,
        String riderId,
        Double pickupLatitude,
        Double pickupLongitude,
        String pickupAddress,
        Double dropLatitude,
        Double dropLongitude,
        String dropAddress
) {
}
