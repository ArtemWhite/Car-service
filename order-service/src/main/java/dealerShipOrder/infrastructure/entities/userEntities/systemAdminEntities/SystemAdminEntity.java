package dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "system_admins")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class SystemAdminEntity extends UserEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_level_id", nullable = false)
    private AdminLevelEntity adminLevel;

    @ManyToMany
    @JoinTable(
            name = "admin_permissions",
            joinColumns = @JoinColumn(name = "admin_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<SystemPermissionEntity> permissions = new ArrayList<>();

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditLogEntryEntity> auditLog = new ArrayList<>();

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}