package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.referencePaymentJpaRepositories;

import dealerShipOrder.infrastructure.entities.paymentEntities.referencePaymentEntities.PaymentStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentStatusReferenceJpaRepository extends JpaRepository<PaymentStatusEntity, String> {

    Optional<PaymentStatusEntity> findByName(String name);
}