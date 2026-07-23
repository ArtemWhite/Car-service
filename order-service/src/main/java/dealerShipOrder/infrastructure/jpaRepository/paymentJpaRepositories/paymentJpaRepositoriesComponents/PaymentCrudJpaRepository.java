package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.paymentEntities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentCrudJpaRepository extends JpaRepository<PaymentEntity, UUID> {
}