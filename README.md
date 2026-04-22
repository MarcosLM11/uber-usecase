# Uber Application

> A microservices-based simulation of how Uber works under the hood — featuring real-time location tracking, event-driven ride matching, and a full ride lifecycle state machine.

---

## Services Overview

| Service | Port | Responsibility |
|---|---|---|
| `location-service` | `8082` | Tracks real-time driver locations via Redis Geospatial |
| `ride-service` | `8083` | Manages ride lifecycle and publishes events to Kafka |
| `matching-service` | `8084` | Consumes ride events, finds and assigns the best driver |

---

## Architecture Flow

```
Driver Phone → Location Service → Redis (GEOADD)

Rider App → Ride Service → Kafka (ride.requested)
                                      |
                                      ↓
                           Matching Service (consumer)
                                      |
                                      ↓
                           Location Service (find nearby drivers)
                                      |
                                      ↓
                           Matching Algorithm (score drivers)
                                      |
                                      ↓
                           Kafka (ride.matched)
                                      |
                                      ↓
                           Ride Service (update ride with driver)
```

---

## Getting Started

### Step 1 — Start Infrastructure

```bash
docker-compose up -d
```

This starts **Redis**, **MySQL**, **Zookeeper**, and **Kafka**.

> Wait ~30 seconds for Kafka to fully initialize before starting the services.

### Step 2 — Start Location Service

```bash
cd location-service
mvn spring-boot:run
```

### Step 3 — Start Ride Service

```bash
cd ride-service
mvn spring-boot:run
```

### Step 4 — Start Matching Service

```bash
cd matching-service
mvn spring-boot:run
```

---

## End-to-End Testing

### 1. Register Driver Locations

```http
POST http://localhost:8082/api/v1/locations/drivers/update
Content-Type: application/json

{ "driverId": "driver:1", "latitude": 12.9716, "longitude": 77.5946 }
```

```http
POST http://localhost:8082/api/v1/locations/drivers/update
Content-Type: application/json

{ "driverId": "driver:2", "latitude": 12.9800, "longitude": 77.5800 }
```

```http
POST http://localhost:8082/api/v1/locations/drivers/update
Content-Type: application/json

{ "driverId": "driver:3", "latitude": 12.9600, "longitude": 77.6100 }
```

### 2. Request a Ride

```http
POST http://localhost:8083/api/v1/rides/request
Content-Type: application/json

{
  "riderId": "rider:1",
  "pickupLatitude": 12.9716,
  "pickupLongitude": 77.5946,
  "pickupAddress": "MG Road, Bangalore",
  "dropLatitude": 12.9352,
  "dropLongitude": 77.6245,
  "dropAddress": "Koramangala, Bangalore"
}
```

### 3. Check Ride Status

```http
GET http://localhost:8083/api/v1/rides/{rideId}
```

The response will show `driverId` assigned and `status: ACCEPTED`.

### 4. Start the Ride

```http
PUT http://localhost:8083/api/v1/rides/{rideId}/start
```

### 5. Complete the Ride

```http
PUT http://localhost:8083/api/v1/rides/{rideId}/complete
```

### 6. Get Rider History

```http
GET http://localhost:8083/api/v1/rides/rider/rider:1
```

---

## Verify Driver Locations in Redis

```bash
docker exec -it redis-geo redis-cli

# List all stored driver locations
ZRANGE drivers:locations 0 -1

# Get coordinates of a specific driver
GEOPOS drivers:locations "driver:1"

# Calculate distance between two drivers
GEODIST drivers:locations "driver:1" "driver:2" km
```

---

## Ride State Machine

```
REQUESTED → MATCHING → ACCEPTED → STARTED → COMPLETED
```

---

## Key Concepts

| Concept | Details |
|---|---|
| Redis Geospatial | `GEOADD` / `GEORADIUS` for real-time driver proximity |
| Kafka | Event-driven communication between services |
| Ride State Machine | Full lifecycle from request to completion |
| Driver Scoring | Weighted algorithm combining distance and rating |
| REST Communication | Matching Service calls Location Service directly |
| Docker Compose | Single command to spin up all infrastructure |

---

## Tech Stack

- **Java** + **Spring Boot**
- **Apache Kafka** — event streaming
- **Redis** — geospatial indexing
- **MySQL** — persistent ride storage
- **Docker Compose** — infrastructure orchestration