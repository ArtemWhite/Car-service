package domain.repository.sparePartRepository;

import domain.models.sparePart.SparePart;
import domain.repository.BaseRepository;

public interface SparePartRepository extends
        BaseRepository<SparePart>,
        SparePartTypeSearch,
        SparePartCompatibilitySearch,
        SparePartStockSearch,
        SparePartPriceSearch,
        SparePartInfoSearch,
        SparePartStockManagement{
}
