package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.managerJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.managerEntities.ManagerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManagerJpaRepository extends JpaRepository<ManagerEntity, UUID> {

    Optional<ManagerEntity> findByIdAndRemovedFalse(UUID id);

    List<ManagerEntity> findAllByRemovedFalse();

    @Query("SELECT m FROM ManagerEntity m WHERE m.available = true AND m.removed = false")
    List<ManagerEntity> findAvailableManagers();

    @Query("SELECT m FROM ManagerEntity m WHERE m.position.name = :position AND m.removed = false")
    List<ManagerEntity> findByPosition(@Param("position") String position);

    @Query("SELECT m FROM ManagerEntity m WHERE SIZE(m.assignedOrderIds) > 0 AND m.removed = false")
    List<ManagerEntity> findManagersWithActiveOrders();
}