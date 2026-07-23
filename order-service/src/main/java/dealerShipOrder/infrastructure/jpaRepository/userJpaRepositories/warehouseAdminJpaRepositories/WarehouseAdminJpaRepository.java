package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.WarehouseAdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseAdminJpaRepository extends JpaRepository<WarehouseAdminEntity, UUID> {

    Optional<WarehouseAdminEntity> findByIdAndRemovedFalse(UUID id);

    List<WarehouseAdminEntity> findAllByRemovedFalse();

    @Query("SELECT w FROM WarehouseAdminEntity w WHERE :sectionId MEMBER OF w.managedSectionIds AND w.removed = false")
    List<WarehouseAdminEntity> findBySection(@Param("sectionId") String sectionId);

    @Query("SELECT w FROM WarehouseAdminEntity w WHERE w.onDuty = true AND w.removed = false")
    List<WarehouseAdminEntity> findOnDutyAdmins();
}