package infrastructure.jpaRepository.sparePartJpaRepositories.referenceSparePartJpaRepositories;


import infrastructure.entities.sparePartEntities.referenceSparePartEntities.SparePartTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpareTypeReferenceJpaRepository extends JpaRepository<SparePartTypeEntity, String> {

    Optional<SparePartTypeEntity> findByName(String name);
}