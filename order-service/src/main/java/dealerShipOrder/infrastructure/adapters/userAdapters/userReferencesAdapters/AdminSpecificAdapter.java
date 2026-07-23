package dealerShipOrder.infrastructure.adapters.userAdapters.userReferencesAdapters;

import dealerShipOrder.domain.models.users.*;
import dealerShipOrder.domain.models.users.systemAdmin.*;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories.SystemAdminJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories.WarehouseAdminJpaRepository;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.systemAdminEntitiesMappers.SystemAdminEntityMapper;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.warehouseAdminEntitiesMappers.WarehouseAdminEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminSpecificAdapter {

    private final SystemAdminJpaRepository systemAdminJpaRepository;
    private final WarehouseAdminJpaRepository warehouseAdminJpaRepository;
    private final SystemAdminEntityMapper systemAdminMapper;
    private final WarehouseAdminEntityMapper warehouseAdminMapper;

    public List<User> findAdminsByLevel(AdminLevel level) {
        return systemAdminJpaRepository.findByAdminLevel(level.name()).stream()
                .map(systemAdminMapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<User> findSystemAdminsWithPermission(String permission) {
        return systemAdminJpaRepository.findByPermission(permission).stream()
                .map(systemAdminMapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<User> findWarehouseAdminsBySection(String sectionId) {
        return warehouseAdminJpaRepository.findBySection(sectionId).stream()
                .map(warehouseAdminMapper::toDomain)
                .collect(Collectors.toList());
    }
}