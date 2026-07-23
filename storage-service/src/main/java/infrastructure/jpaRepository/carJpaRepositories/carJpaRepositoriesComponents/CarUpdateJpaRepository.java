package infrastructure.jpaRepository.carJpaRepositories.carJpaRepositoriesComponents;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.transaction.*;
import java.math.BigDecimal;
import java.util.UUID;

public interface CarUpdateJpaRepository {
    @Modifying
    @Transactional
    @Query("UPDATE CarEntity c SET c.status = :status WHERE c.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE CarEntity c SET c.price = :price WHERE c.id = :id")
    int updatePrice(@Param("id") UUID id, @Param("price") BigDecimal price);

    @Modifying
    @Transactional
    @Query("UPDATE CarEntity c SET c.removed = true WHERE c.id = :id")
    int softDelete(@Param("id") UUID id);
}