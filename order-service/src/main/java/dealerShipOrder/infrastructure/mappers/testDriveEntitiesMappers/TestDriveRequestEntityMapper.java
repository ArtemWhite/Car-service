package dealerShipOrder.infrastructure.mappers.testDriveEntitiesMappers;

import dealerShipOrder.domain.models.testDriveRequest.*;
import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveRequestEntity;
import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveStatusEntity;
import dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveReferenceJpaRepositories.TestDriveStatusReferenceJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class TestDriveRequestEntityMapper {

    @Autowired
    protected TestDriveStatusReferenceJpaRepository testDriveStatusRepository;

    public TestDriveRequestEntity toEntity(TestDriveRequest request) {
        if (request == null) return null;

        TestDriveRequestEntity entity = new TestDriveRequestEntity();
        entity.setId(toUuid(request.getId()));
        entity.setClientId(request.getClientId());
        entity.setCarId(request.getCarId());
        entity.setManagerId(request.getManagerId());
        entity.setRequestedTime(toInstant(request.getRequestedTime()));
        entity.setConfirmedTime(toInstant(request.getConfirmedTime()));
        entity.setStatus(toTestDriveStatusEntity(request.getStatus()));
        entity.setNotes(request.getNotes());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setRemoved(false);

        return entity;
    }

    public TestDriveRequest toDomain(TestDriveRequestEntity entity) {
        if (entity == null) return null;

        TestDriveRequest request = new TestDriveRequest(
                entity.getId().toString(),
                entity.getClientId(),
                entity.getCarId(),
                LocalDateTime.now().plusHours(1)
        );

        request.forceRequestedTime(toLocalDateTime(entity.getRequestedTime()));

        request.setManagerId(entity.getManagerId());
        request.setConfirmedTime(toLocalDateTime(entity.getConfirmedTime()));
        request.setStatus(toTestDriveStatus(entity.getStatus()));
        request.setNotes(entity.getNotes());

        return request;
    }

    protected UUID toUuid(String id) {
        if (id == null) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected String fromUuid(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    protected Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    protected LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    protected TestDriveStatusEntity toTestDriveStatusEntity(TestDriveStatus status) {
        if (status == null) return null;
        return testDriveStatusRepository.findByName(status.name())
                .orElseThrow(() -> new RuntimeException("Test drive status not found: " + status.name()));
    }

    protected TestDriveStatus toTestDriveStatus(TestDriveStatusEntity entity) {
        if (entity == null) return null;
        return TestDriveStatus.valueOf(entity.getName());
    }

    private void restoreManagerId(TestDriveRequest request, String managerId) {
        try {
            Field field = TestDriveRequest.class.getDeclaredField("managerId");
            field.setAccessible(true);
            field.set(request, managerId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore managerId", e);
        }
    }

    private void restoreConfirmedTime(TestDriveRequest request, LocalDateTime confirmedTime) {
        try {
            Field field = TestDriveRequest.class.getDeclaredField("confirmedTime");
            field.setAccessible(true);
            field.set(request, confirmedTime);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore confirmedTime", e);
        }
    }

    private void restoreStatus(TestDriveRequest request, TestDriveStatus status) {
        try {
            Field field = TestDriveRequest.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(request, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore status", e);
        }
    }

    private void restoreNotes(TestDriveRequest request, String notes) {
        try {
            Field field = TestDriveRequest.class.getDeclaredField("notes");
            field.setAccessible(true);
            field.set(request, notes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore notes", e);
        }
    }
}