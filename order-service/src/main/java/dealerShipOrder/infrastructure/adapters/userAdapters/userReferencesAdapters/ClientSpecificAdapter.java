package dealerShipOrder.infrastructure.adapters.userAdapters.userReferencesAdapters;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.infrastructure.entities.userEntities.ClientEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.ClientJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.UserJpaRepository;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.clientEntitiesMappers.ClientEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClientSpecificAdapter {

    private final UserJpaRepository userJpaRepository;
    private final ClientJpaRepository clientJpaRepository;
    private final ClientEntityMapper clientMapper;

    public List<User> findClientsWithOrders() {
        List<ClientEntity> clients = clientJpaRepository.findClientsWithOrders();
        return clients.stream()
                .map(clientMapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<User> findClientsWithTestDrives() {
        List<ClientEntity> clients = clientJpaRepository.findClientsWithTestDrives();
        return clients.stream()
                .map(clientMapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countClientsWithNewsletterSubscription() {
        return clientJpaRepository.countClientsWithNewsletterSubscription();
    }
}