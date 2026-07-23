package infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories;

import infrastructure.entities.carEntities.referenceCarEntities.DriveTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriveTypeJpaRepository extends JpaRepository<DriveTypeEntity, String> {

    Optional<DriveTypeEntity> findByNameAndRemovedFalse(String name);

    List<DriveTypeEntity> findAllByRemovedFalse();
}