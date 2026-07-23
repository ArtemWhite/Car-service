package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.paymentEntities.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentBaseJpaRepository {

    @Query("SELECT p FROM PaymentEntity p WHERE p.id = :id AND p.removed = false")
    Optional<PaymentEntity> findPaymentByIdAndRemovedFalse(@Param("id") UUID id);

    @Query("SELECT p FROM PaymentEntity p WHERE p.removed = false")
    List<PaymentEntity> findAllPaymentsByRemovedFalse();

    @Query("SELECT p FROM PaymentEntity p WHERE p.removed = false")
    Page<PaymentEntity> findAllPaymentsByRemovedFalse(Pageable pageable);

    @Query("SELECT p FROM PaymentEntity p WHERE p.removed = false ORDER BY p.createdAt DESC")
    List<PaymentEntity> findAllPaymentsOrderByCreatedAtDesc();
}