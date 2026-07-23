package infrastructure.jpaRepository.carJpaRepositories.configurationCarJpaRepositories.componentJpaRepositories;

import infrastructure.entities.carEntities.configurationCarEntities.componentEntities.ComponentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComponentTypeJpaRepository extends JpaRepository<ComponentTypeEntity, String> {

    Optional<ComponentTypeEntity> findByNameAndRemovedFalse(String name);

    List<ComponentTypeEntity> findAllByRemovedFalse();
}