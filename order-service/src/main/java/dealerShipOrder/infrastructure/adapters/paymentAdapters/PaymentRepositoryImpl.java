package dealerShipOrder.infrastructure.adapters.paymentAdapters;

import dealerShipOrder.domain.models.payment.*;
import dealerShipOrder.domain.repository.paymentRepository.paymentRepository.PaymentRepository;
import dealerShipOrder.infrastructure.adapters.paymentAdapters.paymentReferencesAdapters.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentBaseRepositoryAdapter baseAdapter;
    private final PaymentClientAdapter clientAdapter;
    private final PaymentOrderAdapter orderAdapter;
    private final PaymentStatusAdapter statusAdapter;
    private final PaymentMethodAdapter methodAdapter;
    private final PaymentDateAdapter dateAdapter;

    @Override
    public Payment save(Payment payment) { return baseAdapter.save(payment); }
    @Override
    public Optional<Payment> findById(String id) { return baseAdapter.findById(id); }
    @Override
    public List<Payment> findAll() { return baseAdapter.findAll(); }
    @Override
    public void delete(String id) { baseAdapter.delete(id); }
    @Override
    public boolean existsById(String id) { return baseAdapter.existsById(id); }

    @Override
    public List<Payment> findByOrderId(String orderId) { return orderAdapter.findByOrderId(orderId); }
    @Override
    public List<Payment> findAllByOrderId(String orderId) { return orderAdapter.findAllByOrderId(orderId); }
    @Override
    public boolean existsByOrderId(String orderId) { return orderAdapter.existsByOrderId(orderId); }

    @Override
    public List<Payment> findByClientId(String clientId) { return clientAdapter.findByClientId(clientId); }
    @Override
    public List<Payment> findByClientIdAndStatus(String clientId, PaymentStatus status) {
        return clientAdapter.findByClientIdAndStatus(clientId, status);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) { return statusAdapter.findByStatus(status); }
    @Override
    public List<Payment> findByStatusIn(List<PaymentStatus> statuses) { return statusAdapter.findByStatusIn(statuses); }
    @Override
    public long countByStatus(PaymentStatus status) { return statusAdapter.countByStatus(status); }

    @Override
    public List<Payment> findByMethod(PaymentMethod method) { return methodAdapter.findByMethod(method); }
    @Override
    public List<Payment> findByMethodAndStatus(PaymentMethod method, PaymentStatus status) {
        return methodAdapter.findByMethodAndStatus(method, status);
    }

    @Override
    public List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return dateAdapter.findByCreatedAtBetween(start, end);
    }
    @Override
    public List<Payment> findByProcessedAtBetween(LocalDateTime start, LocalDateTime end) {
        return dateAdapter.findByProcessedAtBetween(start, end);
    }
    @Override
    public List<Payment> findByDateRangeAndStatus(LocalDateTime start, LocalDateTime end, PaymentStatus status) {
        return dateAdapter.findByDateRangeAndStatus(start, end, status);
    }
}