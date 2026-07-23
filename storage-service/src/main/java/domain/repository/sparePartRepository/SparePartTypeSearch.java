package domain.repository.sparePartRepository;

import domain.models.sparePart.*;

import java.util.List;

public interface SparePartTypeSearch {
    List<SparePart> findByType(SpareType type);
    List<SparePart> findByTypeAndStock(SpareType type, int minQuantity);
    long countByType(SpareType type);
}
