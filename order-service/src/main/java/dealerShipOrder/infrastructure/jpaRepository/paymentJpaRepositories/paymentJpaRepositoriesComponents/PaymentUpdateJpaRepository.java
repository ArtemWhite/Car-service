package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentJpaRepositoriesComponents;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface PaymentUpdateJpaRepository {

    @Modifying
    @Transactional
    @Query("UPDATE PaymentEntity p SET p.status = :status WHERE p.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentEntity p SET p.processedAt = :processedAt, p.transactionId = :transactionId WHERE p.id = :id")
    int markProcessed(@Param("id") UUID id,
                      @Param("processedAt") Instant processedAt,
                      @Param("transactionId") String transactionId);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentEntity p SET p.status = 'FAILED', p.failureReason = :reason WHERE p.id = :id")
    int markFailed(@Param("id") UUID id, @Param("reason") String reason);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentEntity p SET p.status = 'REFUNDED' WHERE p.id = :id")
    int markRefunded(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentEntity p SET p.removed = true WHERE p.id = :id")
    int softDelete(@Param("id") UUID id);
}