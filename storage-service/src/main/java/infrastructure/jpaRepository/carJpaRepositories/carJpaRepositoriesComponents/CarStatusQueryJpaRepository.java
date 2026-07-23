package infrastructure.jpaRepository.carJpaRepositories.carJpaRepositoriesComponents;

import infrastructure.entities.carEntities.CarEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CarStatusQueryJpaRepository  {
    @Query("SELECT c FROM CarEntity c WHERE c.status.name = :status AND c.removed = false")
    List<CarEntity> findCarsByStatus(String status);

    @Query("SELECT c FROM CarEntity c WHERE c.status.name = 'AVAILABLE' AND c.removed = false")
    List<CarEntity> findAvailableCars();
}