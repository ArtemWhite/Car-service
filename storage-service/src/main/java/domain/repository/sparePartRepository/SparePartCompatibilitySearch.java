package domain.repository.sparePartRepository;

import domain.models.car.CarModel;
import domain.models.sparePart.*;

import java.util.List;

public interface SparePartCompatibilitySearch {
    List<SparePart> findByCompatibleModel(CarModel model);
    List<SparePart> findByCompatibleModelAndType(CarModel model, SpareType type);
    boolean isCompatibleWithModel(String partId, String modelId);

    List<SparePart> findByCompatibleModelWithStock(CarModel model,
                                                   java.util.Map<String, Integer> stockMap,
                                                   java.util.Map<String, String> sectionMap,
                                                   java.util.Map<String, String> locationMap);
}
