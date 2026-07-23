package infrastructure.jpaRepository.sparePartJpaRepositories.sparePartJpaRepositoriesComponents;

import infrastructure.entities.sparePartEntities.SparePartEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SparePartCompatibilityJpaRepository {

    @Query("SELECT s FROM SparePartEntity s JOIN s.compatibilities c WHERE c.carModel.id = :modelId AND s.removed = false")
    List<SparePartEntity> findByCompatibleModel(@Param("modelId") UUID modelId);

    @Query("SELECT s FROM SparePartEntity s JOIN s.compatibilities c " +
            "WHERE c.carModel.id = :modelId AND s.type.name = :type AND s.removed = false")
    List<SparePartEntity> findByCompatibleModelAndType(@Param("modelId") UUID modelId,
                                                       @Param("type") String type);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM SparePartCompatibilityEntity c " +
            "WHERE c.sparePart.id = :partId AND c.carModel.id = :modelId AND c.removed = false")
    boolean isCompatibleWithModel(@Param("partId") UUID partId, @Param("modelId") String modelId);

    @Query("SELECT s FROM SparePartEntity s WHERE s.removed = false AND EXISTS " +
            "(SELECT c FROM SparePartCompatibilityEntity c WHERE c.sparePart = s AND c.carModel.id = :modelId)")
    List<SparePartEntity> findCompatibleSpareParts(@Param("modelId") UUID modelId);

    @Query("SELECT s, s.stockQuantity, s.sectionId, s.location FROM SparePartEntity s " +
            "JOIN s.compatibilities c WHERE c.carModel.id = :modelId AND s.removed = false")
    List<Object[]> findCompatibleSparePartsWithStock(@Param("modelId") UUID modelId);
}