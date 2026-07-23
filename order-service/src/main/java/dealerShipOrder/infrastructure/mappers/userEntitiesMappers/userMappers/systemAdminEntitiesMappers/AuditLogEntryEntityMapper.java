package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.systemAdminEntitiesMappers;

import dealerShipOrder.domain.models.users.systemAdmin.AuditLogEntry;
import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.AuditLogEntryEntity;
import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.SystemAdminEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories.SystemAdminJpaRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class})
public abstract class AuditLogEntryEntityMapper {

    @Autowired
    protected SystemAdminJpaRepository systemAdminRepository;

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "admin", expression = "java(toSystemAdminEntity(entry.getAdminId()))")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "details", source = "details")
    @Mapping(target = "timestamp", expression = "java(toInstant(entry.getTimestamp()))")
    @Mapping(target = "removed", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract AuditLogEntryEntity toEntity(AuditLogEntry entry);

    @Mapping(target = "adminId", expression = "java(entity.getAdmin().getId().toString())")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "details", source = "details")
    @Mapping(target = "timestamp", expression = "java(toLocalDateTime(entity.getTimestamp()))")
    public abstract AuditLogEntry toDomain(AuditLogEntryEntity entity);

    protected SystemAdminEntity toSystemAdminEntity(String adminId) {
        if (adminId == null) return null;
        try {
            UUID uuid = UUID.fromString(adminId);
            return systemAdminRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("System admin not found: " + adminId));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid admin ID format: " + adminId);
        }
    }

    protected Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    protected LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}