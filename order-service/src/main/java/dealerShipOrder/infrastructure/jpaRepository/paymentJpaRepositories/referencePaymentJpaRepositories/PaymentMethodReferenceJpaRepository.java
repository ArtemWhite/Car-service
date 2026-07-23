package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.referencePaymentJpaRepositories;

import dealerShipOrder.infrastructure.entities.paymentEntities.referencePaymentEntities.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentMethodReferenceJpaRepository extends JpaRepository<PaymentMethodEntity, String> {

    Optional<PaymentMethodEntity> findByName(String name);
}