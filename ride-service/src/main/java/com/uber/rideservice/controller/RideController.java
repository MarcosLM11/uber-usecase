package com.uber.rideservice.controller;

import com.uber.rideservice.dto.RideRequest;
import com.uber.rideservice.dto.RideResponse;
import com.uber.rideservice.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService service;

    //Rider request a new ride
    @PostMapping("/request")
    public ResponseEntity<RideResponse> requestRide(@Valid @RequestBody RideRequest request) {
        log.info("Ride request recived from rider: {}", request.riderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.requestRide(request));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponse> getRideById(@PathVariable String rideId){
        return ResponseEntity.ok(service.getRideById(rideId));
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<List<RideResponse>> getRidesByRider(@PathVariable String riderId){
        return ResponseEntity.ok(service.getRidesByRider(riderId));
    }

    //Driver starts the ride
    @PutMapping("/{rideId}/start")
    public ResponseEntity<RideResponse> startRide(@PathVariable String rideId){
        return ResponseEntity.ok(service.startRide(rideId));
    }

    @PutMapping("/{rideId}/complete")
    public ResponseEntity<RideResponse> completeRide(@PathVariable String rideId){
        return ResponseEntity.ok(service.completeRide(rideId));
    }

    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<RideResponse> cancelRide(@PathVariable String rideId){
        return ResponseEntity.ok(service.cancelRide(rideId));
    }

}
