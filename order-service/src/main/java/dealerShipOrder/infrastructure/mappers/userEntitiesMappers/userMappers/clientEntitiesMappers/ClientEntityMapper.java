package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.clientEntitiesMappers;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.infrastructure.entities.userEntities.ClientEntity;
import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.BaseUserEntityMapper;
import org.mapstruct.*;

import java.lang.reflect.Field;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ClientEntityMapper extends BaseUserEntityMapper {


    public ClientEntity toEntity(Client client) {
        if (client == null) return null;

        ClientEntity entity = new ClientEntity();
        fillBaseUserEntity(entity, client);
        entity.setPreferredContactMethod(client.getPreferredContactMethod());
        entity.setNewsletterSubscribed(client.isNewsletterSubscribed());
        entity.setOrderIds(toListOfStrings(client.getOrderHistory()));
        entity.setTestDriveRequestIds(toListOfStrings(client.getTestDriveRequests()));
        return entity;
    }

    public Client toDomain(ClientEntity entity) {
        if (entity == null) return null;

        Client client = new Client(
                entity.getId().toString(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getMiddleName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getPasswordHash()
        );

        restorePreferredContactMethod(client, entity.getPreferredContactMethod());
        restoreNewsletterSubscribed(client, entity.isNewsletterSubscribed());
        restoreOrderIds(client, entity.getOrderIds());
        restoreTestDriveRequestIds(client, entity.getTestDriveRequestIds());

        return client;
    }

    public ClientEntity toEntity(User user) {
        if (user instanceof Client client) {
            return toEntity(client);
        }
        throw new IllegalArgumentException("Expected Client, got: " + user.getClass());
    }

    public Client toDomain(UserEntity entity) {
        if (entity instanceof ClientEntity clientEntity) {
            return toDomain(clientEntity);
        }
        throw new IllegalArgumentException("Expected ClientEntity, got: " + entity.getClass());
    }

    private void restorePreferredContactMethod(Client client, String method) {
        if (method != null) {
            client.setPreferredContactMethod(method.toLowerCase());
        }
    }

    private void restoreNewsletterSubscribed(Client client, boolean subscribed) {
        try {
            Field field = Client.class.getDeclaredField("newsletterSubscribed");
            field.setAccessible(true);
            field.set(client, subscribed);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore newsletterSubscribed", e);
        }
    }

    private void restoreOrderIds(Client client, List<String> orderIds) {
        try {
            Field field = Client.class.getDeclaredField("orderIds");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) field.get(client);
            list.clear();
            if (orderIds != null) {
                list.addAll(orderIds);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore orderIds", e);
        }
    }

    private void restoreTestDriveRequestIds(Client client, List<String> testDriveIds) {
        try {
            Field field = Client.class.getDeclaredField("testDriveRequestIds");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) field.get(client);
            list.clear();
            if (testDriveIds != null) {
                list.addAll(testDriveIds);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore testDriveRequestIds", e);
        }
    }
}