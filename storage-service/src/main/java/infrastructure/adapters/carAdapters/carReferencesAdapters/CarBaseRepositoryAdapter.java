package infrastructure.adapters.carAdapters.carReferencesAdapters;

import domain.models.car.Car;
import infrastructure.entities.carEntities.CarEntity;
import infrastructure.jpaRepository.carJpaRepositories.CarJpaRepository;
import infrastructure.mappers.carEntitiesMappers.CarEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CarBaseRepositoryAdapter {

    private final CarJpaRepository jpaRepository;
    private final CarEntityMapper mapper;

    public Car save(Car car) {
        CarEntity entity = mapper.toEntity(car);

        if (entity.getCarInfo() == null || entity.getCarInfo().isBlank()) {
            entity.setCarInfo(car.getCarInfo());
        }

        CarEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Optional<Car> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            Optional<CarEntity> entityOpt = jpaRepository.findCarByIdAndRemovedFalse(uuid);

            if (entityOpt.isPresent()) {
                CarEntity entity = entityOpt.get();
                System.out.println("Found car entity with id: " + entity.getId());
                System.out.println("Car status in entity: " + (entity.getStatus() != null ? entity.getStatus().getName() : "null"));

                Car car = mapper.toDomain(entity);
                System.out.println("Car mapped to domain, test drive available: " + car.isAvailableForTestDrive());
                return Optional.of(car);
            }
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format: " + id);
            return Optional.empty();
        }
    }

    public List<Car> findAll() {
        return jpaRepository.findAllCarsByRemovedFalse().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            jpaRepository.softDelete(uuid);
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    public boolean existsById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.findById(uuid).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}