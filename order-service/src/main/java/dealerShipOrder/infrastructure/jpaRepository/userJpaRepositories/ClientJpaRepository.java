package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {

    Optional<ClientEntity> findByIdAndRemovedFalse(UUID id);

    List<ClientEntity> findAllByRemovedFalse();

    @Query("SELECT c FROM ClientEntity c WHERE SIZE(c.orderIds) > 0 AND c.removed = false")
    List<ClientEntity> findClientsWithOrders();

    @Query("SELECT c FROM ClientEntity c WHERE SIZE(c.testDriveRequestIds) > 0 AND c.removed = false")
    List<ClientEntity> findClientsWithTestDrives();

    @Query("SELECT COUNT(c) FROM ClientEntity c WHERE c.newsletterSubscribed = true AND c.removed = false")
    long countClientsWithNewsletterSubscription();
}