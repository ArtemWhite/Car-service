package dealerShipOrder.infrastructure.adapters.testDriveAdapters.testDriveReferencesAdapters;

import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
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
public class TestDriveConflictAdapter {

    private final TestDriveJpaRepository jpaRepository;
    private final TestDriveRequestEntityMapper mapper;

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public List<TestDriveRequest> findConflictingRequests(String carId, LocalDateTime time) {
        Instant timeStart = toInstant(time.minusHours(1));
        Instant timeEnd = toInstant(time.plusHours(1));
        return jpaRepository.findConflictingRequests(carId, timeStart, timeEnd).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findConflictingForManager(String managerId, LocalDateTime time) {
        Instant timeStart = toInstant(time.minusHours(1));
        Instant timeEnd = toInstant(time.plusHours(1));
        return jpaRepository.findConflictingForManager(managerId, timeStart, timeEnd).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public boolean hasConflict(String carId, LocalDateTime time) {
        return jpaRepository.hasConflict(carId, toInstant(time));
    }
}