package infrastructure.jpaRepository.carJpaRepositories.carJpaRepositoriesComponents;

import infrastructure.entities.carEntities.CarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CarCrudJpaRepository extends JpaRepository<CarEntity, UUID> {

    @Query(value = "SELECT * FROM cars c WHERE c.id = CAST(:id AS uuid)", nativeQuery = true)
    Optional<CarEntity> findByIdAsString(@Param("id") String id);
}