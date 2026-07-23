package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories;

import dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentJpaRepositoriesComponents.*;
import dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentUserJpaRepositories.PaymentClientJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentJpaRepository extends
        PaymentCrudJpaRepository,
        PaymentBaseJpaRepository,
        PaymentStatusJpaRepository,
        PaymentOrderJpaRepository,
        PaymentClientJpaRepository,
        PaymentMethodJpaRepository,
        PaymentDateJpaRepository,
        PaymentStatisticsJpaRepository,
        PaymentUpdateJpaRepository {
}