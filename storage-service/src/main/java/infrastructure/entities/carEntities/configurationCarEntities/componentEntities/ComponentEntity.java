package infrastructure.entities.carEntities.configurationCarEntities.componentEntities;

import javax.persistence.*;

import infrastructure.entities.BaseEntity;
import infrastructure.entities.carEntities.referenceCarEntities.CarModelEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "components")
@Getter
@Setter
public class ComponentEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private ComponentTypeEntity type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "extra_charge", precision = 19, scale = 2)
    private BigDecimal extraCharge;

    @ManyToMany
    @JoinTable(
            name = "component_compatible_models",
            joinColumns = @JoinColumn(name = "component_id"),
            inverseJoinColumns = @JoinColumn(name = "model_id")
    )
    private List<CarModelEntity> compatibleModels = new ArrayList<>();
}