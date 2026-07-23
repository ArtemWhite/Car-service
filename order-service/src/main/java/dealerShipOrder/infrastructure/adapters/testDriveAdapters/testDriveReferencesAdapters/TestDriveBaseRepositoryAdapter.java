package dealerShipOrder.infrastructure.adapters.testDriveAdapters.testDriveReferencesAdapters;

import dealerShipOrder.domain.models.testDriveRequest.*;
import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveRequestEntity;
import dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.TestDriveJpaRepository;
import dealerShipOrder.infrastructure.mappers.testDriveEntitiesMappers.TestDriveRequestEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TestDriveBaseRepositoryAdapter {

    private final TestDriveJpaRepository jpaRepository;
    private final TestDriveRequestEntityMapper mapper;

    public TestDriveRequest save(TestDriveRequest request) {
        TestDriveRequestEntity entity = mapper.toEntity(request);
        TestDriveRequestEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Optional<TestDriveRequest> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.findTestDriveByIdAndRemovedFalse(uuid)
                    .map(mapper::toDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public List<TestDriveRequest> findAll() {
        return jpaRepository.findAllTestDrivesByRemovedFalse().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            jpaRepository.softDelete(uuid);
        } catch (IllegalArgumentException e) {
        }
    }

    public boolean existsById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.existsById(uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}