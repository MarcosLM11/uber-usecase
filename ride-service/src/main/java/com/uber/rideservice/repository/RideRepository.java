package com.uber.rideservice.repository;

import com.uber.rideservice.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, String> {

    List<Ride> findByRiderIdOrderByCreatedAtDesc(String riderId);
}
