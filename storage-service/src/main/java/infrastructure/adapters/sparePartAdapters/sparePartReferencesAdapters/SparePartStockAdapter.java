package infrastructure.adapters.sparePartAdapters.sparePartReferencesAdapters;

import domain.models.sparePart.SparePart;
import infrastructure.jpaRepository.sparePartJpaRepositories.SparePartJpaRepository;
import infrastructure.mappers.sparePartEntitiesMappers.SparePartEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SparePartStockAdapter {

    private final SparePartJpaRepository jpaRepository;
    private final SparePartEntityMapper mapper;

    public List<SparePart> findByStockQuantity(int minQuantity) {
        return jpaRepository.findByStockQuantity(minQuantity).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<SparePart> findOutOfStock() {
        return jpaRepository.findOutOfStock().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<SparePart> findLowStock(int threshold) {
        return jpaRepository.findLowStock(threshold).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<SparePart> findBySection(String sectionId) {
        return jpaRepository.findBySection(sectionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}