package dealerShipOrder.infrastructure.adapters.userAdapters.userReferencesAdapters;

import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.WarehouseAdminEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories.WarehouseAdminJpaRepository;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.warehouseAdminEntitiesMappers.WarehouseAdminEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WarehouseAdminSpecificAdapter {

    private final WarehouseAdminJpaRepository jpaRepository;
    private final WarehouseAdminEntityMapper mapper;

    public Optional<WarehouseAdmin> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.findByIdAndRemovedFalse(uuid)
                    .map(mapper::toDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public List<WarehouseAdmin> findAll() {
        return jpaRepository.findAllByRemovedFalse().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public WarehouseAdmin save(WarehouseAdmin admin) {
        WarehouseAdminEntity entity = mapper.toEntity(admin);
        WarehouseAdminEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public void delete(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            jpaRepository.deleteById(uuid);
        } catch (IllegalArgumentException e) {

        }
    }

    public boolean existsById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.existsById(uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public List<WarehouseAdmin> findBySection(String sectionId) {
        return jpaRepository.findBySection(sectionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<WarehouseAdmin> findOnDutyAdmins() {
        return jpaRepository.findOnDutyAdmins().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}