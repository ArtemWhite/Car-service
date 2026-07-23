package infrastructure.jpaRepository.sparePartJpaRepositories.sparePartJpaRepositoriesComponents;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public interface SparePartUpdateJpaRepository {

    @Modifying
    @Transactional
    @Query("UPDATE SparePartEntity s SET s.price = :price WHERE s.id = :id")
    int updatePrice(@Param("id") UUID id, @Param("price") BigDecimal price);

    @Modifying
    @Transactional
    @Query("UPDATE SparePartEntity s SET s.stockQuantity = :quantity WHERE s.id = :id")
    int updateStockQuantity(@Param("id") UUID id, @Param("quantity") int quantity);

    @Modifying
    @Transactional
    @Query("UPDATE SparePartEntity s SET s.stockQuantity = s.stockQuantity + :delta WHERE s.id = :id")
    int addToStock(@Param("id") UUID id, @Param("delta") int delta);

    @Modifying
    @Transactional
    @Query("UPDATE SparePartEntity s SET s.sectionId = :sectionId, s.location = :location WHERE s.id = :id")
    int updateLocation(@Param("id") UUID id,
                       @Param("sectionId") String sectionId,
                       @Param("location") String location);

    @Modifying
    @Transactional
    @Query("UPDATE SparePartEntity s SET s.removed = true WHERE s.id = :id")
    int softDelete(@Param("id") UUID id);
}