package infrastructure.jpaRepository.sparePartJpaRepositories.sparePartJpaRepositoriesComponents;

import infrastructure.entities.sparePartEntities.SparePartEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SparePartBaseJpaRepository {

    @Query("SELECT s FROM SparePartEntity s WHERE s.id = :id AND s.removed = false")
    Optional<SparePartEntity> findSparePartByIdAndRemovedFalse(@Param("id") UUID id);

    @Query("SELECT s FROM SparePartEntity s WHERE s.removed = false")
    List<SparePartEntity> findAllSparePartsByRemovedFalse();

    @Query("SELECT s FROM SparePartEntity s WHERE s.removed = false")
    Page<SparePartEntity> findAllSparePartsByRemovedFalse(Pageable pageable);

    @Query("SELECT s FROM SparePartEntity s WHERE s.removed = false ORDER BY s.name ASC")
    List<SparePartEntity> findAllSparePartsOrderByName();
}