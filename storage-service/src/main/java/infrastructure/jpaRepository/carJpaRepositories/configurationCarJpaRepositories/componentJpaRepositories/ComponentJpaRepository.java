package infrastructure.jpaRepository.carJpaRepositories.configurationCarJpaRepositories.componentJpaRepositories;

import infrastructure.entities.carEntities.configurationCarEntities.componentEntities.ComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComponentJpaRepository extends JpaRepository<ComponentEntity, String> {

    Optional<ComponentEntity> findByIdAndRemovedFalse(String id);

    List<ComponentEntity> findAllByRemovedFalse();

    @Query("SELECT c FROM ComponentEntity c WHERE c.type.name = :type AND c.removed = false")
    List<ComponentEntity> findByType(@Param("type") String type);

    @Query("SELECT c FROM ComponentEntity c JOIN c.compatibleModels m WHERE m.id = :modelId AND c.removed = false")
    List<ComponentEntity> findCompatibleWithModel(@Param("modelId") String modelId);
}