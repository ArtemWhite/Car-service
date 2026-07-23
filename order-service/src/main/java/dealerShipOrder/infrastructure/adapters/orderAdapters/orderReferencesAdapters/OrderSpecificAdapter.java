package dealerShipOrder.infrastructure.adapters.orderAdapters.orderReferencesAdapters;

import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.OrderJpaRepository;
import dealerShipOrder.infrastructure.mappers.orderEntitiesMappers.OrderEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderSpecificAdapter {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    public Optional<Order> findByCarId(String carId) {
        return jpaRepository.findByCarId(carId).stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    public List<Order> findByCarModelId(String carModelId) {
        return jpaRepository.findByCarModelId(carModelId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Order> findByConfigurationId(String configurationId) {
        return jpaRepository.findByConfigurationId(configurationId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public boolean existsActiveOrderForCar(String carId) {
        return jpaRepository.existsActiveOrderForCar(carId);
    }

    public List<Order> findByOrderType(String orderType) {
        return jpaRepository.findByOrderType(orderType).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}