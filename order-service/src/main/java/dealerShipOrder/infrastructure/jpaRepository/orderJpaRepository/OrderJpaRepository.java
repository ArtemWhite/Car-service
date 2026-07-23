package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository;

import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderJpaRepositoriesComponents.*;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderUserJpaRepositories.OrderClientJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderUserJpaRepositories.OrderManagerJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderJpaRepository extends
        OrderCrudJpaRepository,
        OrderBaseJpaRepository,
        OrderStatusJpaRepository,
        OrderClientJpaRepository,
        OrderManagerJpaRepository,
        OrderDateJpaRepository,
        OrderSpecificJpaRepository,
        OrderStatisticsJpaRepository,
        OrderUpdateJpaRepository,
        OrderWithFiltersJpaRepository{
}