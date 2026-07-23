package domain.repository;

import domain.models.assembly.AssemblyOrder;
import domain.models.assembly.AssemblyOrderStatus;

import java.util.List;
import java.util.Optional;

public interface AssemblyOrderRepository {
    Optional<AssemblyOrder> findById(String id);
    List<AssemblyOrder> findAll();
    AssemblyOrder save(AssemblyOrder order);
    void delete(String id);
    List<AssemblyOrder> findBySourceOrderId(String sourceOrderId);
    List<AssemblyOrder> findByStatus(AssemblyOrderStatus status);
}