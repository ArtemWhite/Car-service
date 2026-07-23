package infrastructure.jpaRepository.sparePartJpaRepositories.sparePartJpaRepositoriesComponents;

import infrastructure.entities.sparePartEntities.SparePartEntity;
import infrastructure.entities.sparePartEntities.referenceSparePartEntities.SparePartTypeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SparePartTypeJpaRepository {

    @Query("SELECT s FROM SparePartEntity s WHERE s.type.name = :type AND s.removed = false")
    List<SparePartEntity> findByType(@Param("type") String type);

    @Query("SELECT s FROM SparePartEntity s WHERE s.type.name = :type AND s.stockQuantity >= :minQuantity AND s.removed = false")
    List<SparePartEntity> findByTypeAndMinStock(@Param("type") String type,
                                                @Param("minQuantity") int minQuantity);

    @Query("SELECT COUNT(s) FROM SparePartEntity s WHERE s.type.name = :type AND s.removed = false")
    long countByType(@Param("type") String type);

    @Query("SELECT s.type.name, COUNT(s) FROM SparePartEntity s WHERE s.removed = false GROUP BY s.type.name")
    List<Object[]> countSparePartsByType();

    @Query("SELECT t FROM SparePartTypeEntity t WHERE t.name = :name AND t.removed = false")
    Optional<SparePartTypeEntity> findSpareTypeByName(@Param("name") String name);
}