package dealerShipOrder.presentation.controllers;

import com.dealershipOrder.grpc.CarResponse;
import dealerShipOrder.infrastructure.grpc.CarGrpcClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
@Tag(name = "Cars", description = "Car information from StorageService via gRPC")
public class CarController {

    private final CarGrpcClient carGrpcClient;

    @GetMapping
    @Operation(summary = "Get all available cars")
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> getAvailableCars() {
        log.info("REST request: GET /api/v1/cars");

        try {
            List<CarResponse> cars = carGrpcClient.getAvailableCars();

            return ResponseEntity.ok(Map.of(
                    "cars", cars,
                    "total_count", cars.size()
            ));

        } catch (RuntimeException e) {
            log.error("Failed to get cars from StorageService", e);

            if (e.getMessage().contains("timeout")) {
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                        .body(Map.of("error", "Storage service timeout", "message", e.getMessage()));
            }
            if (e.getMessage().contains("unavailable")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Storage service unavailable", "message", e.getMessage()));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal error", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car by ID")
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> getCarById(@PathVariable String id) {
        log.info("REST request: GET /api/v1/cars/{}", id);

        try {
            CarResponse car = carGrpcClient.getCarById(id);

            if (car == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Car not found", "id", id));
            }

            return ResponseEntity.ok(car);

        } catch (RuntimeException e) {
            log.error("Failed to get car {} from StorageService", id, e);

            if (e.getMessage().contains("timeout")) {
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                        .body(Map.of("error", "Storage service timeout", "message", e.getMessage()));
            }
            if (e.getMessage().contains("unavailable")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Storage service unavailable", "message", e.getMessage()));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal error", "message", e.getMessage()));
        }
    }
}