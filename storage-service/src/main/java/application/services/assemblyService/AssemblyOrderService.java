package application.services.assemblyService;

import domain.models.assembly.AssemblyOrder;
import domain.models.assembly.AssemblyOrderStatus;
import domain.models.car.Car;
import domain.models.car.CarConfiguration;
import domain.repository.AssemblyOrderRepository;
import domain.repository.carRepository.CarRepository;
import domain.repository.carRepository.ConfigurationRepository;
import domain.repository.sparePartRepository.SparePartRepository;
import events.OrderApprovedEvent;
import events.OrderRejectedEvent;
import events.OrderSentForApprovalEvent;
import infrastructure.messaging.StorageEventPublisher;
import infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssemblyOrderService {

    private final AssemblyOrderRepository assemblyOrderRepository;
    private final CarRepository carRepository;
    private final ConfigurationRepository configurationRepository;
    private final SparePartRepository sparePartRepository;
    private final StorageEventPublisher eventPublisher;

    @Transactional
    public void processOrderApproval(OrderSentForApprovalEvent event) {
        log.info("Processing order approval for orderId: {}", event.getOrderId());

        String responsibleAdminId = SecurityUtils.getCurrentUserId();

        AssemblyOrder assemblyOrder = AssemblyOrder.create(
                event.getOrderId(),
                event.getOrderType(),
                event.getCarId(),
                event.getConfigurationId(),
                event.getCarModelId(),
                responsibleAdminId
        );

        assemblyOrder = assemblyOrderRepository.save(assemblyOrder);
        log.info("AssemblyOrder created with id: {}", assemblyOrder.getId());

        try {
            boolean canAssemble = checkAvailability(event);

            if (canAssemble) {
                assemblyOrder.assemble();
                assemblyOrderRepository.save(assemblyOrder);

                OrderApprovedEvent approvedEvent = new OrderApprovedEvent(
                        event.getOrderId(),
                        event.getTraceId(),
                        assemblyOrder.getId(),
                        "Assembly completed successfully"
                );
                eventPublisher.publishOrderApproved(approvedEvent);

                log.info("Order {} approved, assemblyOrderId: {}", event.getOrderId(), assemblyOrder.getId());
            } else {
                assemblyOrder.fail();
                assemblyOrderRepository.save(assemblyOrder);

                OrderRejectedEvent rejectedEvent = new OrderRejectedEvent(
                        event.getOrderId(),
                        event.getTraceId(),
                        assemblyOrder.getId(),
                        "Not enough components in stock"
                );
                eventPublisher.publishOrderRejected(rejectedEvent);

                log.warn("Order {} rejected - not enough components", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Error processing order approval for orderId: {}", event.getOrderId(), e);

            assemblyOrder.fail();
            assemblyOrderRepository.save(assemblyOrder);

            OrderRejectedEvent rejectedEvent = new OrderRejectedEvent(
                    event.getOrderId(),
                    event.getTraceId(),
                    assemblyOrder.getId(),
                    "Error: " + e.getMessage()
            );
            eventPublisher.publishOrderRejected(rejectedEvent);
        }
    }

    private boolean checkAvailability(OrderSentForApprovalEvent event) {
        if ("IN_STOCK".equals(event.getOrderType())) {
            return checkCarAvailability(event.getCarId());
        } else if ("CUSTOM".equals(event.getOrderType())) {
            return checkComponentsAvailability(event.getConfigurationId());
        }
        return false;
    }

    private boolean checkCarAvailability(String carId) {
        return carRepository.findById(carId)
                .map(Car::isAvailableForPurchase)
                .orElse(false);
    }

    private boolean checkComponentsAvailability(String configurationId) {
        return configurationRepository.findById(configurationId)
                .map(this::checkAllComponentsInStock)
                .orElse(false);
    }

    private boolean checkAllComponentsInStock(CarConfiguration configuration) {
        List<String> componentIds = configuration.getBaseComponents().values().stream()
                .map(component -> component.getId())
                .toList();

        for (String componentId : componentIds) {
            int stockQuantity = sparePartRepository.getStockQuantity(componentId);
            if (stockQuantity <= 0) {
                log.warn("Component {} is out of stock", componentId);
                return false;
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public AssemblyOrder findById(String id) {
        return assemblyOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AssemblyOrder not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<AssemblyOrder> findAll() {
        return assemblyOrderRepository.findAll();
    }

    @Transactional
    public AssemblyOrder create(AssemblyOrder assemblyOrder) {
        assemblyOrder.setStatus(AssemblyOrderStatus.CREATED);
        return assemblyOrderRepository.save(assemblyOrder);
    }

    @Transactional
    public AssemblyOrder update(String id, AssemblyOrder updated) {
        AssemblyOrder existing = findById(id);
        existing.setStatus(updated.getStatus());
        existing.setResponsibleWarehouseAdminId(updated.getResponsibleWarehouseAdminId());
        return assemblyOrderRepository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        assemblyOrderRepository.delete(id);
    }
}