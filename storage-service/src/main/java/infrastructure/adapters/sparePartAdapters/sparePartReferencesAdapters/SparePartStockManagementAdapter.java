package infrastructure.adapters.sparePartAdapters.sparePartReferencesAdapters;

import infrastructure.jpaRepository.sparePartJpaRepositories.SparePartJpaRepository;
import infrastructure.entities.sparePartEntities.SparePartEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SparePartStockManagementAdapter {

    private final SparePartJpaRepository jpaRepository;

    public int getStockQuantity(String sparePartId) {
        try {
            UUID uuid = UUID.fromString(sparePartId);
            return jpaRepository.findById(uuid)
                    .map(SparePartEntity::getStockQuantity)
                    .orElse(0);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public void updateStock(String sparePartId, int newQuantity, String sectionId, String location) {
        try {
            UUID uuid = UUID.fromString(sparePartId);
            SparePartEntity entity = jpaRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("Spare part not found: " + sparePartId));
            entity.setStockQuantity(newQuantity);
            if (sectionId != null) entity.setSectionId(sectionId);
            if (location != null) entity.setLocation(location);
            jpaRepository.save(entity);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid spare part ID format: " + sparePartId);
        }
    }

    public String getSectionId(String sparePartId) {
        try {
            UUID uuid = UUID.fromString(sparePartId);
            return jpaRepository.findById(uuid)
                    .map(SparePartEntity::getSectionId)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getLocation(String sparePartId) {
        try {
            UUID uuid = UUID.fromString(sparePartId);
            return jpaRepository.findById(uuid)
                    .map(SparePartEntity::getLocation)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}