package infrastructure.jpaRepository.carJpaRepositories;

import infrastructure.jpaRepository.carJpaRepositories.carJpaRepositoriesComponents.*;
import org.springframework.stereotype.Repository;

@Repository
public interface CarJpaRepository extends
        CarCrudJpaRepository,
        CarBaseJpaRepository,
        CarStatusQueryJpaRepository,
        CarCharacteristicJpaRepository,
        CarPriceJpaRepository,
        CarEngineJpaRepository,
        CarFilterJpaRepository,
        CarStatisticsJpaRepository,
        CarUpdateJpaRepository {
}

