package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.clientEntitiesMappers;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.infrastructure.entities.userEntities.ClientEntity;
import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.BaseUserEntityMapper;
import org.mapstruct.*;

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

        return new Client(
                entity.getId().toString(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getMiddleName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getPasswordHash()
        );
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
}