package dealerShipOrder.infrastructure.adapters.testDriveAdapters.testDriveReferencesAdapters;

import dealerShipOrder.domain.models.testDriveRequest.*;
import dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.TestDriveJpaRepository;
import dealerShipOrder.infrastructure.mappers.testDriveEntitiesMappers.TestDriveRequestEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TestDriveManagerAdapter {

    private final TestDriveJpaRepository jpaRepository;
    private final TestDriveRequestEntityMapper mapper;

    public List<TestDriveRequest> findByManagerId(String managerId) {
        return jpaRepository.findByManagerId(managerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findByManagerIdAndStatus(String managerId, TestDriveStatus status) {
        return jpaRepository.findByManagerIdAndStatus(managerId, status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findUpcomingByManagerId(String managerId) {
        return jpaRepository.findUpcomingByManagerId(managerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countAssignedToManager(String managerId) {
        return jpaRepository.countAssignedToManager(managerId);
    }
}