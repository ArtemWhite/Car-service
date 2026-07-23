package infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories;

import infrastructure.entities.carEntities.referenceCarEntities.CarStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarStatusJpaRepository extends JpaRepository<CarStatusEntity, String> {

    Optional<CarStatusEntity> findByNameAndRemovedFalse(String name);

    List<CarStatusEntity> findAllByRemovedFalse();
}