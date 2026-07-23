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
public class TestDriveClientAdapter {

    private final TestDriveJpaRepository jpaRepository;
    private final TestDriveRequestEntityMapper mapper;

    public List<TestDriveRequest> findByClientId(String clientId) {
        return jpaRepository.findByClientId(clientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findByClientIdAndStatus(String clientId, TestDriveStatus status) {
        return jpaRepository.findByClientIdAndStatus(clientId, status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<TestDriveRequest> findUpcomingByClientId(String clientId) {
        return jpaRepository.findUpcomingByClientId(clientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public boolean hasActiveRequestForClient(String clientId) {
        return jpaRepository.hasActiveRequestForClient(clientId);
    }

    public long countByClientId(String clientId) {
        return jpaRepository.countByClientId(clientId);
    }
}