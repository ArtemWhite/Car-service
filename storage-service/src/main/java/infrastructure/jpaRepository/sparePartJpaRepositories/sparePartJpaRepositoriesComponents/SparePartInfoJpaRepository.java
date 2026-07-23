package infrastructure.jpaRepository.sparePartJpaRepositories.sparePartJpaRepositoriesComponents;

import infrastructure.entities.sparePartEntities.SparePartEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SparePartInfoJpaRepository {

    @Query("SELECT s FROM SparePartEntity s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.removed = false")
    List<SparePartEntity> findByNameContaining(@Param("name") String name);

    @Query("SELECT s FROM SparePartEntity s WHERE s.manufacturer = :manufacturer AND s.removed = false")
    List<SparePartEntity> findByManufacturer(@Param("manufacturer") String manufacturer);

    @Query("SELECT s FROM SparePartEntity s WHERE s.partNumber = :partNumber AND s.removed = false")
    Optional<SparePartEntity> findByPartNumber(@Param("partNumber") String partNumber);

    @Query("SELECT s FROM SparePartEntity s WHERE LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) AND s.removed = false")
    List<SparePartEntity> findByDescriptionContaining(@Param("keyword") String keyword);
}