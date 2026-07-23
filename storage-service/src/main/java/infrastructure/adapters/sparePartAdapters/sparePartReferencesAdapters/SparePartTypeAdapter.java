package infrastructure.adapters.sparePartAdapters.sparePartReferencesAdapters;

import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import infrastructure.jpaRepository.sparePartJpaRepositories.SparePartJpaRepository;
import infrastructure.mappers.sparePartEntitiesMappers.SparePartEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SparePartTypeAdapter {

    private final SparePartJpaRepository jpaRepository;
    private final SparePartEntityMapper mapper;

    public List<SparePart> findByType(SpareType type) {
        return jpaRepository.findByType(type.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<SparePart> findByTypeAndStock(SpareType type, int minQuantity) {
        return jpaRepository.findByTypeAndMinStock(type.name(), minQuantity).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countByType(SpareType type) {
        return jpaRepository.countByType(type.name());
    }
}