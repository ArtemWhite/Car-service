package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.SystemAdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemAdminJpaRepository extends JpaRepository<SystemAdminEntity, UUID> {

    Optional<SystemAdminEntity> findByIdAndRemovedFalse(UUID id);

    List<SystemAdminEntity> findAllByRemovedFalse();

    @Query("SELECT s FROM SystemAdminEntity s WHERE s.adminLevel.name = :level AND s.removed = false")
    List<SystemAdminEntity> findByAdminLevel(@Param("level") String level);

    @Query("SELECT s FROM SystemAdminEntity s WHERE :permission MEMBER OF s.permissions AND s.removed = false")
    List<SystemAdminEntity> findByPermission(@Param("permission") String permission);
}