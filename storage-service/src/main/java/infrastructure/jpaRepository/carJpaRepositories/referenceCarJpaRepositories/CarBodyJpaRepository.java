package infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories;

import infrastructure.entities.carEntities.referenceCarEntities.CarBodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarBodyJpaRepository extends JpaRepository<CarBodyEntity, String> {

    Optional<CarBodyEntity> findByNameAndRemovedFalse(String name);

    List<CarBodyEntity> findAllByRemovedFalse();
}