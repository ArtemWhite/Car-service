package infrastructure.jpaRepository.carJpaRepositories.technicalCarJpaRepositories.EngineJpaRepositories;

import infrastructure.entities.carEntities.technicalCarEntities.engineEntities.EngineFuelTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EngineFuelTypeJpaRepository extends JpaRepository<EngineFuelTypeEntity, String> {

    Optional<EngineFuelTypeEntity> findByNameAndRemovedFalse(String name);

    List<EngineFuelTypeEntity> findAllByRemovedFalse();
}