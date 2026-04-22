package com.uber.locationservice.model.dto;

public record DriverLocationRequest(
        String driverId,
        Double latitude,
        Double longitude
) {
}
