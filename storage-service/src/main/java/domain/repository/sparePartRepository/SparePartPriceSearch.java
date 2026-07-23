package domain.repository.sparePartRepository;

import domain.models.car.Price;
import domain.models.sparePart.SparePart;

import java.util.List;

public interface SparePartPriceSearch {
    List<SparePart> findByPriceRange(Price minPrice, Price maxPrice);
    List<SparePart> findByPriceLessThan(Price maxPrice);
}
