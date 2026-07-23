package domain.repository.sparePartRepository;

import domain.models.sparePart.SparePart;

import java.util.List;
import java.util.Optional;

public interface SparePartInfoSearch {
    List<SparePart> findByNameContaining(String name);
    List<SparePart> findByManufacturer(String manufacturer);
    Optional<SparePart> findByPartNumber(String partNumber);
}
