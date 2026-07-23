package infrastructure.adapters.sparePartAdapters.sparePartReferencesAdapters;

import domain.models.car.Price;
import domain.models.sparePart.SparePart;
import infrastructure.jpaRepository.sparePartJpaRepositories.SparePartJpaRepository;
import infrastructure.mappers.sparePartEntitiesMappers.SparePartEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SparePartPriceAdapter {

    private final SparePartJpaRepository jpaRepository;
    private final SparePartEntityMapper mapper;

    public List<SparePart> findByPriceRange(Price minPrice, Price maxPrice) {
        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice.getAmount().doubleValue()) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice.getAmount().doubleValue()) : null;

        if (min != null && max != null) {
            return jpaRepository.findByPriceRange(min, max).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        } else if (max != null) {
            return jpaRepository.findByPriceLessThan(max).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public List<SparePart> findByPriceLessThan(Price maxPrice) {
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice.getAmount().doubleValue()) : null;
        if (max != null) {
            return jpaRepository.findByPriceLessThan(max).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}