package infrastructure.entities.carEntities.technicalCarEntities.engineEntities;

import org.hibernate.annotations.Where;

import javax.persistence.*;

import infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "engine_fuel_types")
@Where(clause = "removed = false")
@Getter
@Setter
public class EngineFuelTypeEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
}