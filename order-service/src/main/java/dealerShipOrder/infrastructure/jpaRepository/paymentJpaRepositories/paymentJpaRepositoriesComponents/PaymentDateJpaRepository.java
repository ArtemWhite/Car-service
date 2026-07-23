package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.paymentEntities.PaymentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PaymentDateJpaRepository {

    @Query("SELECT p FROM PaymentEntity p WHERE p.createdAt BETWEEN :start AND :end AND p.removed = false")
    List<PaymentEntity> findByCreatedAtBetween(@Param("start") Instant start,
                                               @Param("end") Instant end);

    @Query("SELECT p FROM PaymentEntity p WHERE p.processedAt BETWEEN :start AND :end AND p.removed = false")
    List<PaymentEntity> findByProcessedAtBetween(@Param("start") Instant start,
                                                 @Param("end") Instant end);

    @Query("SELECT p FROM PaymentEntity p WHERE p.createdAt BETWEEN :start AND :end AND p.status.name = :status AND p.removed = false")
    List<PaymentEntity> findByDateRangeAndStatus(@Param("start") Instant start,
                                                 @Param("end") Instant end,
                                                 @Param("status") String status);

    @Query("SELECT p FROM PaymentEntity p WHERE DATE(p.createdAt) = :date AND p.removed = false")
    List<PaymentEntity> findByCreatedAtDate(@Param("date") Instant date);
}