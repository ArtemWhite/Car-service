package infrastructure.jpaRepository.sparePartJpaRepositories.referenceSparePartJpaRepositories;

import infrastructure.entities.sparePartEntities.referenceSparePartEntities.SparePartCompatibilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface SparePartCompatibilityRelationJpaRepository extends JpaRepository<SparePartCompatibilityEntity, UUID> {

    @Query("SELECT c FROM SparePartCompatibilityEntity c WHERE c.sparePart.id = :sparePartId AND c.removed = false")
    List<SparePartCompatibilityEntity> findBySparePartId(@Param("sparePartId") UUID sparePartId);

    @Query("SELECT c FROM SparePartCompatibilityEntity c WHERE c.carModel.id = :modelId AND c.removed = false")
    List<SparePartCompatibilityEntity> findByCarModelId(@Param("modelId") String modelId);

    @Query("SELECT c FROM SparePartCompatibilityEntity c WHERE c.sparePart.id = :sparePartId AND c.carModel.id = :modelId AND c.removed = false")
    List<SparePartCompatibilityEntity> findBySparePartAndModel(@Param("sparePartId") UUID sparePartId,
                                                               @Param("modelId") String modelId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SparePartCompatibilityEntity c WHERE c.sparePart.id = :sparePartId AND c.carModel.id = :modelId")
    int deleteCompatibility(@Param("sparePartId") UUID sparePartId, @Param("modelId") String modelId);

    @Query("SELECT COUNT(c) FROM SparePartCompatibilityEntity c WHERE c.sparePart.id = :sparePartId AND c.removed = false")
    long countCompatibleModels(@Param("sparePartId") UUID sparePartId);
}