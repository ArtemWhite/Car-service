package infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories;

import infrastructure.entities.carEntities.referenceCarEntities.CarColorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CarColorJpaRepository extends JpaRepository<CarColorEntity, String> {

    Optional<CarColorEntity> findByNameAndRemovedFalse(String name);

    List<CarColorEntity> findAllByRemovedFalse();

    @Query("SELECT c FROM CarColorEntity c WHERE c.isDefault = true AND c.removed = false")
    List<CarColorEntity> findDefaultColors();
}