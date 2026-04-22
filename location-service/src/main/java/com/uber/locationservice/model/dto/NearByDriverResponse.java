package com.uber.locationservice.model.dto;

public record NearByDriverResponse(
        String driverId,
        Double latitude,
        Double longitude,
        Double distance //km
) {
}
