package infrastructure.adapters;

import domain.models.assembly.AssemblyOrder;
import domain.models.assembly.AssemblyOrderStatus;
import domain.repository.AssemblyOrderRepository;
import infrastructure.entities.AssemblyOrderEntity;
import infrastructure.jpaRepository.AssemblyOrderJpaRepository;
import infrastructure.mappers.AssemblyOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AssemblyOrderRepositoryImpl implements AssemblyOrderRepository {

    private final AssemblyOrderJpaRepository jpaRepository;
    private final AssemblyOrderMapper mapper;

    @Override
    public Optional<AssemblyOrder> findById(String id) {
        return jpaRepository.findByIdAndRemovedFalse(UUID.fromString(id))
                .map(mapper::toDomain);
    }

    @Override
    public List<AssemblyOrder> findAll() {
        return jpaRepository.findAllByRemovedFalse().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public AssemblyOrder save(AssemblyOrder order) {
        AssemblyOrderEntity entity = mapper.toEntity(order);
        AssemblyOrderEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(String id) {
        jpaRepository.findById(UUID.fromString(id)).ifPresent(entity -> {
            entity.setRemoved(true);
            jpaRepository.save(entity);
        });
    }

    @Override
    public List<AssemblyOrder> findBySourceOrderId(String sourceOrderId) {
        return jpaRepository.findBySourceOrderId(sourceOrderId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssemblyOrder> findByStatus(AssemblyOrderStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}