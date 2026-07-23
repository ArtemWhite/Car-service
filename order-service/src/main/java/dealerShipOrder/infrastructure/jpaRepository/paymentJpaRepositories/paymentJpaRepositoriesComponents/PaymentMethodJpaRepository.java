package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.paymentEntities.PaymentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentMethodJpaRepository {

    @Query("SELECT p FROM PaymentEntity p WHERE p.method.name = :method AND p.removed = false")
    List<PaymentEntity> findByMethod(@Param("method") String method);

    @Query("SELECT p FROM PaymentEntity p WHERE p.method.name = :method AND p.status.name = :status AND p.removed = false")
    List<PaymentEntity> findByMethodAndStatus(@Param("method") String method,
                                              @Param("status") String status);
}