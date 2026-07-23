package dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "system_permissions")
@Getter
@Setter
public class SystemPermissionEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "category", length = 50)
    private String category;
}