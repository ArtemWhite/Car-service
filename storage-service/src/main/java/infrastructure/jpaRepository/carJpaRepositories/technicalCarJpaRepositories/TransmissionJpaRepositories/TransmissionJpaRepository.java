package infrastructure.jpaRepository.carJpaRepositories.technicalCarJpaRepositories.TransmissionJpaRepositories;

import infrastructure.entities.carEntities.technicalCarEntities.transmissionEntity.TransmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransmissionJpaRepository extends JpaRepository<TransmissionEntity, String> {

    Optional<TransmissionEntity> findByIdAndRemovedFalse(String id);

    List<TransmissionEntity> findAllByRemovedFalse();

    @Query("SELECT t FROM TransmissionEntity t WHERE t.type.name = :type AND t.removed = false")
    List<TransmissionEntity> findByType(@Param("type") String type);

    @Query("SELECT t FROM TransmissionEntity t WHERE t.gears = :gears AND t.removed = false")
    List<TransmissionEntity> findByGears(@Param("gears") int gears);

    @Query("SELECT t FROM TransmissionEntity t WHERE t.type.name = :type AND t.gears = :gears AND t.removed = false")
    Optional<TransmissionEntity> findByTypeAndGears(@Param("type") String type, @Param("gears") int gears);
}