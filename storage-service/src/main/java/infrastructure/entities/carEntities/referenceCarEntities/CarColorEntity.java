package infrastructure.entities.carEntities.referenceCarEntities;

import org.hibernate.annotations.Where;

import javax.persistence.*;

import infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "car_colors")
@Where(clause = "removed = false")
@Getter
@Setter
public class CarColorEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Column(name = "color_price")
    private int colorPrice = 0;
}