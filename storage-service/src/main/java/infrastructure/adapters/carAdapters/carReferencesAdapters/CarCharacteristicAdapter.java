package infrastructure.adapters.carAdapters.carReferencesAdapters;

import domain.models.car.Car;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.types.CarBrand;
import infrastructure.jpaRepository.carJpaRepositories.CarJpaRepository;
import infrastructure.mappers.carEntitiesMappers.CarEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CarCharacteristicAdapter {

    private final CarJpaRepository jpaRepository;
    private final CarEntityMapper mapper;

    public List<Car> findByBrand(CarBrand brand) {
        return jpaRepository.findByBrand(brand.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Car> findByModel(CarModel model) {
        return jpaRepository.findByModel(model.getName()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Car> findByPriceRange(Price minPrice, Price maxPrice) {
        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice.getAmount().doubleValue()) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice.getAmount().doubleValue()) : null;

        if (min != null && max != null) {
            return jpaRepository.findByPriceRange(min, max).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        } else if (min != null) {
            return jpaRepository.findByPriceGreaterThan(min).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        } else if (max != null) {
            return jpaRepository.findByPriceLessThan(max).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public List<Car> findByDriveType(String driveType) {
        return jpaRepository.findByDriveType(driveType).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Car> findByColor(String color) {
        return jpaRepository.findByColor(color).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Car> findByBody(String body) {
        return jpaRepository.findByBody(body).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Car> findByBrandAndModel(String brand, String model) {
        return jpaRepository.findByBrandAndModel(brand, model).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Object[]> countCarsByBrand() {
        return jpaRepository.countCarsByBrand();
    }
}