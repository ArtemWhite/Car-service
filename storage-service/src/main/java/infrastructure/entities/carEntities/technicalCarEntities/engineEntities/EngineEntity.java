package infrastructure.entities.carEntities.technicalCarEntities.engineEntities;

import javax.persistence.*;

import infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "engines")
@Getter
@Setter
public class EngineEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fuel_type_id", nullable = false)
    private EngineFuelTypeEntity fuelType;

    @Column(name = "displacement")
    private double displacement;

    @Column(name = "horse_power")
    private double horsePower;

    @Column(length = 500)
    private String description;
}