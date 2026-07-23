package infrastructure.adapters.carAdapters.carReferencesAdapters;

import domain.models.car.Car;
import domain.repository.carRepository.CarEngineSearch;
import infrastructure.jpaRepository.carJpaRepositories.CarJpaRepository;
import infrastructure.mappers.carEntitiesMappers.CarEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CarEngineAdapter implements CarEngineSearch {

    private final CarJpaRepository jpaRepository;
    private final CarEntityMapper mapper;

    @Override
    public List<Car> findByEngineFuelType(String fuelType) {
        return jpaRepository.findByEngineFuelType(fuelType).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Car> findByEnginePowerRange(double minPower, double maxPower) {
        return jpaRepository.findByEnginePowerRange(minPower, maxPower).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Car> findByEngineDisplacementRange(double minVolume, double maxVolume) {
        return jpaRepository.findByEngineDisplacementRange(minVolume, maxVolume).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}