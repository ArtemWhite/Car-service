package infrastructure.adapters.sparePartAdapters.sparePartReferencesAdapters;

import domain.models.sparePart.SparePart;
import infrastructure.jpaRepository.sparePartJpaRepositories.SparePartJpaRepository;
import infrastructure.mappers.sparePartEntitiesMappers.SparePartEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SparePartInfoAdapter {

    private final SparePartJpaRepository jpaRepository;
    private final SparePartEntityMapper mapper;

    public List<SparePart> findByNameContaining(String name) {
        return jpaRepository.findByNameContaining(name).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<SparePart> findByManufacturer(String manufacturer) {
        return jpaRepository.findByManufacturer(manufacturer).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public Optional<SparePart> findByPartNumber(String partNumber) {
        return jpaRepository.findByPartNumber(partNumber)
                .map(mapper::toDomain);
    }
}