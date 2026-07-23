package infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories;

import infrastructure.entities.carEntities.referenceCarEntities.CarBrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarBrandJpaRepository extends JpaRepository<CarBrandEntity, String> {

    Optional<CarBrandEntity> findByNameAndRemovedFalse(String name);

    List<CarBrandEntity> findAllByRemovedFalse();

    @Query("SELECT b FROM CarBrandEntity b WHERE b.displayName LIKE %:query% AND b.removed = false")
    List<CarBrandEntity> findByDisplayNameContaining(@Param("query") String query);
}