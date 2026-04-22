package com.uber.rideservice.event;

import lombok.Builder;

/**
 * Event published to kafka topic: ride.matched
 * Consumed by Ride Service to update ride with assigned driver
 */
@Builder
public record RideMatchEvent(
        String rideId,
        String riderId,
        String driverId,
        Double driverLatitude,
        Double driverLongitude,
        Double distanceToPickupKm
) {
}
