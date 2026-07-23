package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.paymentEntities.PaymentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentStatusJpaRepository {

    @Query("SELECT p FROM PaymentEntity p WHERE p.status.name = :status AND p.removed = false")
    List<PaymentEntity> findByStatus(@Param("status") String status);

    @Query("SELECT p FROM PaymentEntity p WHERE p.status.name IN (:statuses) AND p.removed = false")
    List<PaymentEntity> findByStatusIn(@Param("statuses") List<String> statuses);

    @Query("SELECT p FROM PaymentEntity p WHERE p.status.name = 'PENDING' AND p.removed = false")
    List<PaymentEntity> findPendingPayments();

    @Query("SELECT p FROM PaymentEntity p WHERE p.status.name = 'COMPLETED' AND p.removed = false")
    List<PaymentEntity> findCompletedPayments();

    @Query("SELECT p FROM PaymentEntity p WHERE p.status.name = 'FAILED' AND p.removed = false")
    List<PaymentEntity> findFailedPayments();

    @Query("SELECT p FROM PaymentEntity p WHERE p.status.name = 'REFUNDED' AND p.removed = false")
    List<PaymentEntity> findRefundedPayments();

    @Query("SELECT p FROM PaymentEntity p WHERE p.status.name IN ('PENDING', 'PROCESSING') AND p.removed = false")
    List<PaymentEntity> findActivePayments();
}