package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.orderEntities.OrderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderSpecificJpaRepository {

    @Query("SELECT o FROM OrderEntity o WHERE o.carId = :carId AND o.removed = false")
    List<OrderEntity> findByCarId(@Param("carId") String carId);

    @Query("SELECT o FROM OrderEntity o WHERE o.carModelId = :carModelId AND o.removed = false")
    List<OrderEntity> findByCarModelId(@Param("carModelId") String carModelId);

    @Query("SELECT o FROM OrderEntity o WHERE o.configurationId = :configurationId AND o.removed = false")
    List<OrderEntity> findByConfigurationId(@Param("configurationId") String configurationId);

    @Query("SELECT o FROM OrderEntity o WHERE o.type.name = :type AND o.removed = false")
    List<OrderEntity> findByOrderType(@Param("type") String type);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM OrderEntity o " +
            "WHERE o.carId = :carId AND o.status.name NOT IN ('COMPLETED', 'CANCELLED') AND o.removed = false")
    boolean existsActiveOrderForCar(@Param("carId") String carId);
}