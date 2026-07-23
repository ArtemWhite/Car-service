package infrastructure.adapters.carAdapters.carRepositoriesImpls;

import domain.models.car.CarConfiguration;
import domain.repository.carRepository.ConfigurationRepository;
import infrastructure.entities.carEntities.configurationCarEntities.CarConfigurationEntity;
import infrastructure.jpaRepository.carJpaRepositories.configurationCarJpaRepositories.CarConfigurationJpaRepository;
import infrastructure.mappers.carEntitiesMappers.carConfigurationMappers.CarConfigurationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ConfigurationRepositoryImpl implements ConfigurationRepository {

    private final CarConfigurationJpaRepository jpaRepository;
    private final CarConfigurationEntityMapper mapper;

    @Override
    public Optional<CarConfiguration> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.findByIdAndRemovedFalse(uuid).map(mapper::toDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CarConfiguration> findAll() {
        return jpaRepository.findAllByRemovedFalse().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarConfiguration> findByModelId(String modelId) {
        try {
            UUID uuid = UUID.fromString(modelId);
            return jpaRepository.findByModelId(uuid).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @Override
    public Optional<CarConfiguration> findBaseByModelId(String modelId) {
        return findByModelId(modelId).stream().findFirst();
    }

    @Override
    public CarConfiguration save(CarConfiguration configuration) {
        CarConfigurationEntity entity = mapper.toEntity(configuration);
        CarConfigurationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<CarConfiguration> findByPriceRange(double minPrice, double maxPrice) {
        BigDecimal min = BigDecimal.valueOf(minPrice);
        BigDecimal max = BigDecimal.valueOf(maxPrice);
        return jpaRepository.findByPriceRange(min, max).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}