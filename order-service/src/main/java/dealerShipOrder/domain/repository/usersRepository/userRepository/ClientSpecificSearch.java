package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.User;

import java.util.List;

public interface ClientSpecificSearch {
    List<User> findClientsWithOrders();
    List<User> findClientsWithTestDrives();
    long countClientsWithNewsletterSubscription();
}
