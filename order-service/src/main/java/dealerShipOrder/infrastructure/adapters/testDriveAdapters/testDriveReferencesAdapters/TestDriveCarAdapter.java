package dealerShipOrder.infrastructure.adapters.testDriveAdapters.testDriveReferencesAdapters;

import dealerShipOrder.domain.models.testDriveRequest.*;
import dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.TestDriveJpaRepository;
import dealerShipOrder.infrastructure.mappers.testDriveEntitiesMappers.TestDriveRequestEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TestDriveCarAdapter {

    private final TestDriveJpaRepository jpaRepository;
    private final TestDriveRequestEntityMapper mapper;

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public List<TestDriveRequest> findByCarId(String carId) {
        return jpaRepository.findByCarId(carId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findByCarIdAndStatus(String carId, TestDriveStatus status) {
        return jpaRepository.findByCarIdAndStatus(carId, status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findUpcomingByCarId(String carId) {
        return jpaRepository.findUpcomingByCarId(carId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public boolean isCarBookedForTestDrive(String carId, LocalDateTime time) {
        return jpaRepository.isCarBookedForTestDrive(carId, toInstant(time));
    }
}