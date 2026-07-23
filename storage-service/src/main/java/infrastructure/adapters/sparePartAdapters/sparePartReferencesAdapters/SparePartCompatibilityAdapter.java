package infrastructure.adapters.sparePartAdapters.sparePartReferencesAdapters;

import domain.models.car.CarModel;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import infrastructure.entities.sparePartEntities.SparePartEntity;
import infrastructure.jpaRepository.sparePartJpaRepositories.SparePartJpaRepository;
import infrastructure.mappers.sparePartEntitiesMappers.SparePartEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SparePartCompatibilityAdapter {

    private final SparePartJpaRepository jpaRepository;
    private final SparePartEntityMapper mapper;

    public List<SparePart> findByCompatibleModel(CarModel model) {
        try {
            UUID uuid = UUID.fromString(model.getId());
            return jpaRepository.findByCompatibleModel(uuid).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public List<SparePart> findByCompatibleModelAndType(CarModel model, SpareType type) {
        try {
            UUID uuid = UUID.fromString(model.getId());
            return jpaRepository.findByCompatibleModelAndType(uuid, type.name()).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public boolean isCompatibleWithModel(String partId, String modelId) {
        try {
            UUID partUuid = UUID.fromString(partId);
            return jpaRepository.isCompatibleWithModel(partUuid, modelId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public List<SparePart> findByCompatibleModelWithStock(CarModel model,
                                                          java.util.Map<String, Integer> stockMap,
                                                          java.util.Map<String, String> sectionMap,
                                                          java.util.Map<String, String> locationMap) {
        try {
            UUID uuid = UUID.fromString(model.getId());
            List<Object[]> results = jpaRepository.findCompatibleSparePartsWithStock(uuid);

            List<SparePart> spareParts = new ArrayList<>();
            for (Object[] row : results) {
                SparePartEntity entity = (SparePartEntity) row[0];
                Integer stock = ((Number) row[1]).intValue();
                String section = (String) row[2];
                String location = (String) row[3];

                spareParts.add(mapper.toDomain(entity));
                stockMap.put(entity.getId().toString(), stock);
                sectionMap.put(entity.getId().toString(), section);
                locationMap.put(entity.getId().toString(), location);
            }
            return spareParts;
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }
}