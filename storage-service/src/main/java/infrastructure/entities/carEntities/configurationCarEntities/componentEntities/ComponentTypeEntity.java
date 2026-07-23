package infrastructure.entities.carEntities.configurationCarEntities.componentEntities;

import org.hibernate.annotations.Where;

import javax.persistence.*;

import infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "component_types")
@Where(clause = "removed = false")
@Getter
@Setter
public class ComponentTypeEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
}