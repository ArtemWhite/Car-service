package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.paymentEntities.PaymentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentOrderJpaRepository {

    @Query("SELECT p FROM PaymentEntity p WHERE p.orderId = :orderId AND p.removed = false")
    List<PaymentEntity> findByOrderId(@Param("orderId") String orderId);

    @Query("SELECT p FROM PaymentEntity p WHERE p.orderId = :orderId AND p.status.name = :status AND p.removed = false")
    List<PaymentEntity> findByOrderIdAndStatus(@Param("orderId") String orderId,
                                               @Param("status") String status);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PaymentEntity p " +
            "WHERE p.orderId = :orderId AND p.removed = false")
    boolean existsByOrderId(@Param("orderId") String orderId);

    @Query("SELECT p FROM PaymentEntity p WHERE p.orderId = :orderId ORDER BY p.createdAt DESC")
    List<PaymentEntity> findPaymentsByOrderIdDesc(@Param("orderId") String orderId);
}