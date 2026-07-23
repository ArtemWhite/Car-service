package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID>,
        JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByIdAndRemovedFalse(UUID id);

    List<UserEntity> findAllByRemovedFalse();

    Optional<UserEntity> findByEmailAndRemovedFalse(String email);

    boolean existsByEmailAndRemovedFalse(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.userType.name = :userType AND u.removed = false")
    List<UserEntity> findByUserType(@Param("userType") String userType);

    @Query("SELECT u FROM UserEntity u WHERE u.status.name = :status AND u.removed = false")
    List<UserEntity> findByStatus(@Param("status") String status);

    @Query("SELECT u FROM UserEntity u WHERE u.lastActiveAt < :date AND u.removed = false")
    List<UserEntity> findInactiveSince(@Param("date") Instant date);
}