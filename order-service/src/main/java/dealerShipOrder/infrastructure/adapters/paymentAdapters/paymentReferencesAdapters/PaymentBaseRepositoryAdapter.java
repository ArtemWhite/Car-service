package dealerShipOrder.infrastructure.adapters.paymentAdapters.paymentReferencesAdapters;

import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.infrastructure.entities.paymentEntities.PaymentEntity;
import dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.PaymentJpaRepository;
import dealerShipOrder.infrastructure.mappers.paymentEntitiesMappers.PaymentEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentBaseRepositoryAdapter {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentEntityMapper mapper;

    public Payment save(Payment payment) {
        PaymentEntity entity = mapper.toEntity(payment);
        PaymentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Optional<Payment> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.findPaymentByIdAndRemovedFalse(uuid)
                    .map(mapper::toDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public List<Payment> findAll() {
        return jpaRepository.findAllPaymentsByRemovedFalse().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            jpaRepository.softDelete(uuid);
        } catch (IllegalArgumentException e) {

        }
    }

    public boolean existsById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.existsById(uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}