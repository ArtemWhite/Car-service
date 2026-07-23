package infrastructure.entities.carEntities.configurationCarEntities;

import javax.persistence.*;

import infrastructure.entities.BaseEntity;
import infrastructure.entities.carEntities.configurationCarEntities.componentEntities.ComponentEntity;
import infrastructure.entities.carEntities.referenceCarEntities.CarModelEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "car_configurations")
@Getter
@Setter
public class CarConfigurationEntity extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private CarModelEntity model;

    @Column(name = "base_price", precision = 19, scale = 2)
    private BigDecimal basePrice;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "configuration_id")
    private List<ComponentEntity> baseComponents = new ArrayList<>();
}