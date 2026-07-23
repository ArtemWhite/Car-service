package domain.repository.sparePartRepository;

import domain.models.sparePart.SparePart;

import java.util.List;

public interface SparePartStockSearch {
    List<SparePart> findByStockQuantity(int minQuantity);
    List<SparePart> findOutOfStock();
    List<SparePart> findLowStock(int threshold);
    List<SparePart> findBySection(String sectionId);
}
