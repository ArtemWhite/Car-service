package infrastructure.jpaRepository.sparePartJpaRepositories;

import infrastructure.jpaRepository.sparePartJpaRepositories.sparePartJpaRepositoriesComponents.*;
import org.springframework.stereotype.Repository;

@Repository
public interface SparePartJpaRepository extends
        SparePartCrudJpaRepository,
        SparePartBaseJpaRepository,
        SparePartTypeJpaRepository,
        SparePartCompatibilityJpaRepository,
        SparePartStockJpaRepository,
        SparePartPriceJpaRepository,
        SparePartInfoJpaRepository,
        SparePartStatisticsJpaRepository,
        SparePartUpdateJpaRepository {
}