package domain.repository.carRepository;

import domain.models.car.CarConfiguration;

import java.util.List;
import java.util.Optional;

public interface ConfigurationRepository
{
    Optional<CarConfiguration> findById(String id);
    List<CarConfiguration> findAll();
    List<CarConfiguration> findByModelId(String modelId);
    Optional<CarConfiguration> findBaseByModelId(String modelId);
    CarConfiguration save(CarConfiguration configuration);
    List<CarConfiguration> findByPriceRange(double minPrice, double maxPrice);
}
