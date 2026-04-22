package com.uber.matchingservice.dto;

public record NearByDriverResponse(
        String driverId,
        Double latitude,
        Double longitude,
        Double distance //km
) {
}
