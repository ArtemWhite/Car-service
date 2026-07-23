package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentUserJpaRepositories;

import dealerShipOrder.infrastructure.entities.paymentEntities.PaymentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentClientJpaRepository {

    @Query("SELECT p FROM PaymentEntity p WHERE p.clientId = :clientId AND p.removed = false")
    List<PaymentEntity> findByClientId(@Param("clientId") String clientId);

    @Query("SELECT p FROM PaymentEntity p WHERE p.clientId = :clientId AND p.status.name = :status AND p.removed = false")
    List<PaymentEntity> findByClientIdAndStatus(@Param("clientId") String clientId,
                                                @Param("status") String status);

    @Query("SELECT p FROM PaymentEntity p WHERE p.clientId = :clientId ORDER BY p.createdAt DESC")
    List<PaymentEntity> findClientPaymentsByDateDesc(@Param("clientId") String clientId);

    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.clientId = :clientId AND p.status.name = 'COMPLETED' AND p.removed = false")
    Double getTotalSpentByClient(@Param("clientId") String clientId);
}