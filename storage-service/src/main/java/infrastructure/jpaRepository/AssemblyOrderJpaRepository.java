package infrastructure.jpaRepository;

import infrastructure.entities.AssemblyOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssemblyOrderJpaRepository extends JpaRepository<AssemblyOrderEntity, UUID> {

    @Query("SELECT a FROM AssemblyOrderEntity a WHERE a.sourceOrderId = :sourceOrderId AND a.removed = false")
    List<AssemblyOrderEntity> findBySourceOrderId(@Param("sourceOrderId") String sourceOrderId);

    @Query("SELECT a FROM AssemblyOrderEntity a WHERE a.status = :status AND a.removed = false")
    List<AssemblyOrderEntity> findByStatus(@Param("status") String status);

    @Query("SELECT a FROM AssemblyOrderEntity a WHERE a.id = :id AND a.removed = false")
    Optional<AssemblyOrderEntity> findByIdAndRemovedFalse(@Param("id") UUID id);

    @Query("SELECT a FROM AssemblyOrderEntity a WHERE a.removed = false ORDER BY a.createdAt DESC")
    List<AssemblyOrderEntity> findAllByRemovedFalse();
}