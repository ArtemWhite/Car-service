package infrastructure.adapters.sparePartAdapters;

import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import domain.repository.sparePartRepository.SparePartRepository;
import infrastructure.adapters.sparePartAdapters.sparePartReferencesAdapters.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SparePartRepositoryImpl implements SparePartRepository {

    private final SparePartBaseRepositoryAdapter baseAdapter;
    private final SparePartTypeAdapter typeAdapter;
    private final SparePartCompatibilityAdapter compatibilityAdapter;
    private final SparePartStockAdapter stockAdapter;
    private final SparePartPriceAdapter priceAdapter;
    private final SparePartInfoAdapter infoAdapter;
    private final SparePartStockManagementAdapter stockManagementAdapter;

    @Override
    public SparePart save(SparePart sparePart) { return baseAdapter.save(sparePart); }
    @Override
    public Optional<SparePart> findById(String id) { return baseAdapter.findById(id); }
    @Override
    public List<SparePart> findAll() { return baseAdapter.findAll(); }
    @Override
    public void delete(String id) { baseAdapter.delete(id); }
    @Override
    public boolean existsById(String id) { return baseAdapter.existsById(id); }

    @Override
    public List<SparePart> findByType(SpareType type) { return typeAdapter.findByType(type); }
    @Override
    public List<SparePart> findByTypeAndStock(SpareType type, int minQuantity) {
        return typeAdapter.findByTypeAndStock(type, minQuantity);
    }
    @Override
    public long countByType(SpareType type) { return typeAdapter.countByType(type); }

    @Override
    public List<SparePart> findByCompatibleModel(CarModel model) {
        return compatibilityAdapter.findByCompatibleModel(model);
    }
    @Override
    public List<SparePart> findByCompatibleModelAndType(CarModel model, SpareType type) {
        return compatibilityAdapter.findByCompatibleModelAndType(model, type);
    }
    @Override
    public boolean isCompatibleWithModel(String partId, String modelId) {
        return compatibilityAdapter.isCompatibleWithModel(partId, modelId);
    }

    @Override
    public List<SparePart> findByStockQuantity(int minQuantity) { return stockAdapter.findByStockQuantity(minQuantity); }
    @Override
    public List<SparePart> findOutOfStock() { return stockAdapter.findOutOfStock(); }
    @Override
    public List<SparePart> findLowStock(int threshold) { return stockAdapter.findLowStock(threshold); }
    @Override
    public List<SparePart> findBySection(String sectionId) { return stockAdapter.findBySection(sectionId); }

    @Override
    public List<SparePart> findByPriceRange(Price minPrice, Price maxPrice) {
        return priceAdapter.findByPriceRange(minPrice, maxPrice);
    }
    @Override
    public List<SparePart> findByPriceLessThan(Price maxPrice) {
        return priceAdapter.findByPriceLessThan(maxPrice);
    }

    @Override
    public List<SparePart> findByNameContaining(String name) { return infoAdapter.findByNameContaining(name); }
    @Override
    public List<SparePart> findByManufacturer(String manufacturer) { return infoAdapter.findByManufacturer(manufacturer); }
    @Override
    public Optional<SparePart> findByPartNumber(String partNumber) { return infoAdapter.findByPartNumber(partNumber); }

    @Override
    public int getStockQuantity(String sparePartId) { return stockManagementAdapter.getStockQuantity(sparePartId); }
    @Override
    public void updateStock(String sparePartId, int newQuantity, String sectionId, String location) {
        stockManagementAdapter.updateStock(sparePartId, newQuantity, sectionId, location);
    }

    @Override
    public List<SparePart> findByCompatibleModelWithStock(CarModel model,
                                                          java.util.Map<String, Integer> stockMap,
                                                          java.util.Map<String, String> sectionMap,
                                                          java.util.Map<String, String> locationMap) {
        return compatibilityAdapter.findByCompatibleModelWithStock(model, stockMap, sectionMap, locationMap);
    }

    @Override
    public String getSectionId(String sparePartId) {
        return stockManagementAdapter.getSectionId(sparePartId);
    }

    @Override
    public String getLocation(String sparePartId) {
        return stockManagementAdapter.getLocation(sparePartId);
    }
}