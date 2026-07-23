package infrastructure.jpaRepository.sparePartJpaRepositories.sparePartJpaRepositoriesComponents;

import infrastructure.entities.sparePartEntities.SparePartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SparePartCrudJpaRepository extends JpaRepository<SparePartEntity, UUID> {
}