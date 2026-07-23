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
public class TestDriveStatusAdapter {

    private final TestDriveJpaRepository jpaRepository;
    private final TestDriveRequestEntityMapper mapper;

    public List<TestDriveRequest> findByStatus(TestDriveStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findByStatusIn(List<TestDriveStatus> statuses) {
        List<String> statusNames = statuses.stream()
                .map(TestDriveStatus::name)
                .collect(Collectors.toList());
        return jpaRepository.findByStatusIn(statusNames).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countByStatus(TestDriveStatus status) {
        return jpaRepository.countByStatus(status.name());
    }
}