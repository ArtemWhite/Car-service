package infrastructure.jpaRepository.carJpaRepositories.carJpaRepositoriesComponents;

import infrastructure.entities.carEntities.CarEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarBaseJpaRepository {

    @Query("SELECT c FROM CarEntity c WHERE c.id = :id AND c.removed = false")
    Optional<CarEntity> findCarByIdAndRemovedFalse(@Param("id") UUID id);

    @Query(value = "SELECT * FROM cars c WHERE c.id = CAST(:id AS uuid) AND c.removed = false", nativeQuery = true)
    Optional<CarEntity> findCarByIdAsStringAndRemovedFalse(@Param("id") String id);

    @Query("SELECT c FROM CarEntity c WHERE c.removed = false")
    List<CarEntity> findAllCarsByRemovedFalse();

    @Query("SELECT c FROM CarEntity c WHERE c.removed = false")
    Page<CarEntity> findAllCarsByRemovedFalse(Pageable pageable);
}