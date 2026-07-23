package infrastructure.jpaRepository.carJpaRepositories.technicalCarJpaRepositories.TransmissionJpaRepositories;

import infrastructure.entities.carEntities.technicalCarEntities.transmissionEntity.TransmissionTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransmissionTypeJpaRepository extends JpaRepository<TransmissionTypeEntity, String> {

    Optional<TransmissionTypeEntity> findByNameAndRemovedFalse(String name);

    List<TransmissionTypeEntity> findAllByRemovedFalse();
}