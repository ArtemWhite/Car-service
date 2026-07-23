package infrastructure.adapters.carAdapters.carReferencesAdapters;

import domain.models.car.Car;
import domain.models.car.types.CarStatus;
import infrastructure.jpaRepository.carJpaRepositories.CarJpaRepository;
import infrastructure.mappers.carEntitiesMappers.CarEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CarStatusAdapter {

    private final CarJpaRepository jpaRepository;
    private final CarEntityMapper mapper;

    public List<Car> findByStatus(CarStatus status) {
        return jpaRepository.findCarsByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Car> findAvailableCars() {
        return jpaRepository.findAvailableCars().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Car> findCarsForTestDrive() {
        return jpaRepository.findCarsByStatus(CarStatus.TEST_DRIVE_AVAILABLE.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countByStatus(CarStatus status) {
        return jpaRepository.countByStatus(status.name());
    }

    public long countAvailableCars() {
        return jpaRepository.countByStatus(CarStatus.AVAILABLE.name());
    }
}