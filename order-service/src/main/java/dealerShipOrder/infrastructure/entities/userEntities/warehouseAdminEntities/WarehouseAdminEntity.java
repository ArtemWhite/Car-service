package dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "warehouse_admins")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class WarehouseAdminEntity extends UserEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_position_id")
    private WarehousePositionEntity position;

    @Column(name = "on_duty")
    private boolean onDuty;

    @ElementCollection
    @CollectionTable(name = "warehouse_admin_sections", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "section_id")
    private Set<String> managedSectionIds = new HashSet<>();

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockOperationEntity> operationHistory = new ArrayList<>();
}