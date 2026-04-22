package com.uber.rideservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="riders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ride {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column(nullable = false)
    private String riderId; //Who requested the ride
    @Column(nullable = false)
    private String driverId; //Who accepted the ride (null until matched)
    @Column(nullable = false)
    private Double pickupLatitude;
    @Column(nullable = false)
    private Double pickupLongitude;
    @Column(nullable = false)
    private String pickupAddress;
    @Column(nullable = false)
    private Double dropLatitude;
    @Column(nullable = false)
    private Double dropLongitude;
    @Column(nullable = false)
    private String dropAddress;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status; //tracks lifecycle
    private Double estimatedFare;
    private Double actualFare;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
