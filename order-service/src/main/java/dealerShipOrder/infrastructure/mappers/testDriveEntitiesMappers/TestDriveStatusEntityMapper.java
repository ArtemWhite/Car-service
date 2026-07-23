package dealerShipOrder.infrastructure.mappers.testDriveEntitiesMappers;

import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveStatusEntity;
import dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveReferenceJpaRepositories.TestDriveStatusReferenceJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class TestDriveStatusEntityMapper {

    @Autowired
    protected TestDriveStatusReferenceJpaRepository testDriveStatusRepository;

    public TestDriveStatusEntity toEntity(TestDriveStatus status) {
        if (status == null) return null;
        return testDriveStatusRepository.findByName(status.name())
                .orElseThrow(() -> new RuntimeException("Test drive status not found: " + status.name()));
    }

    public TestDriveStatus toDomain(TestDriveStatusEntity entity) {
        if (entity == null) return null;
        return TestDriveStatus.valueOf(entity.getName());
    }
}