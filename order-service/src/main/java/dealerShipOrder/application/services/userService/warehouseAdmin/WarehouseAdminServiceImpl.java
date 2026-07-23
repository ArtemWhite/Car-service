package dealerShipOrder.application.services.userService.warehouseAdmin;

import dealerShipOrder.application.dtos.request.userRequest.ChangePasswordRequest;
import dealerShipOrder.application.dtos.request.userRequest.OperationHistoryRequest;
import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.response.userResponse.users.WarehouseAdminResponse;
import dealerShipOrder.application.mapper.UserMapper;
import dealerShipOrder.application.services.userService.BaseUserService;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.warehouseAdmin.OperationType;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseAdminServiceImpl extends BaseUserService implements WarehouseAdminService {

    public WarehouseAdminServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper) {
        super(userRepository, userMapper);
    }

    private WarehouseAdmin getCurrentWarehouseAdmin() {
        String adminId = SecurityUtils.getCurrentUserId();
        User user = findUserById(adminId);
        if (!(user instanceof WarehouseAdmin warehouseAdmin)) {
            throw new DomainValidationException("User is not a warehouse admin");
        }
        return warehouseAdmin;
    }

    @Override
    public WarehouseAdminResponse updateOwnProfile(UpdateUserRequest request) {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();
        request.setWarehousePosition(null);
        userMapper.updateDomain(admin, request);
        WarehouseAdmin updated = (WarehouseAdmin) saveUser(admin);
        return userMapper.toWarehouseAdminResponse(updated);
    }

    @Override
    public WarehouseAdminResponse changeOwnPassword(ChangePasswordRequest request) {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();
        if (!admin.authenticate(request.getOldPassword())) {
            throw new DomainValidationException("Old password is incorrect");
        }
        admin.changePassword(request.getOldPassword(), request.getNewPassword());
        WarehouseAdmin updated = (WarehouseAdmin) saveUser(admin);
        return userMapper.toWarehouseAdminResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseAdminResponse getUserById(String id) {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();
        return userMapper.toWarehouseAdminResponse(admin);
    }

    @Override
    public WarehouseAdminResponse assignToSection(String sectionId) {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();
        admin.assignToSection(sectionId);
        admin.addOperation("ASSIGN_SECTION", "Assigned to section: " + sectionId);
        WarehouseAdmin updated = (WarehouseAdmin) saveUser(admin);
        return userMapper.toWarehouseAdminResponse(updated);
    }

    @Override
    public WarehouseAdminResponse removeFromSection(String sectionId) {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();
        admin.removeFromSection(sectionId);
        admin.addOperation("REMOVE_SECTION", "Removed from section: " + sectionId);
        WarehouseAdmin updated = (WarehouseAdmin) saveUser(admin);
        return userMapper.toWarehouseAdminResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getManagedSections() {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();
        return admin.getManagedSectionIds();
    }

    @Override
    public WarehouseAdminResponse startShift() {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();
        admin.startShift();
        WarehouseAdmin updated = (WarehouseAdmin) saveUser(admin);
        return userMapper.toWarehouseAdminResponse(updated);
    }

    @Override
    public WarehouseAdminResponse endShift() {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();
        admin.endShift();
        WarehouseAdmin updated = (WarehouseAdmin) saveUser(admin);
        return userMapper.toWarehouseAdminResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOnDuty() {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();
        return admin.isOnDuty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationHistoryRequest> getOperationHistory() {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();

        return admin.getOperationHistory().stream()
                .map(op -> OperationHistoryRequest.builder()
                        .id(op.getId())
                        .operationType(op.getType().name())
                        .description(op.getDescription())
                        .timestamp(op.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationHistoryRequest> getOperationsByDate(String from, String to) {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();

        LocalDateTime fromDate = LocalDateTime.parse(from, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime toDate = LocalDateTime.parse(to, DateTimeFormatter.ISO_DATE_TIME);

        return admin.getOperationsByDate(fromDate, toDate).stream()
                .map(op -> OperationHistoryRequest.builder()
                        .id(op.getId())
                        .operationType(op.getType().name())
                        .description(op.getDescription())
                        .timestamp(op.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationHistoryRequest> getOperationsByType(String operationType) {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();

        return admin.getOperationsByType(OperationType.valueOf(operationType)).stream()
                .map(op -> OperationHistoryRequest.builder()
                        .id(op.getId())
                        .operationType(op.getType().name())
                        .description(op.getDescription())
                        .timestamp(op.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseAdminResponse getMyProfile() {
        WarehouseAdmin admin = getCurrentWarehouseAdmin();
        return userMapper.toWarehouseAdminResponse(admin);
    }
}