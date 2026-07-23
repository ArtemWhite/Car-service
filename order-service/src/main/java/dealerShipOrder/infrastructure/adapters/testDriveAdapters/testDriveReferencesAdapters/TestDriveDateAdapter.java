package dealerShipOrder.infrastructure.adapters.testDriveAdapters.testDriveReferencesAdapters;

import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
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
public class TestDriveDateAdapter {

    private final TestDriveJpaRepository jpaRepository;
    private final TestDriveRequestEntityMapper mapper;

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public List<TestDriveRequest> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByDateRange(toInstant(start), toInstant(end)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findUpcomingTestDrives() {
        return jpaRepository.findUpcomingTestDrives().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findPastTestDrives() {
        return jpaRepository.findPastTestDrives().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findByDateAndStatus(LocalDateTime date, TestDriveStatus status) {
        return jpaRepository.findByDateAndStatus(toInstant(date), status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findByDateTimeBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByDateTimeBetween(toInstant(start), toInstant(end)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}