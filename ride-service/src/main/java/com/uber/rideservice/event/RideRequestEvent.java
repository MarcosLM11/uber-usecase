package com.uber.rideservice.event;

import lombok.Builder;

/**
 * Event published to kafka when a ride is requested
 * Matching service consumed this event
 * TOPIC: ride.requested
 */
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
