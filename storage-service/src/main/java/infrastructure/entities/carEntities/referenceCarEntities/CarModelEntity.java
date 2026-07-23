package infrastructure.entities.carEntities.referenceCarEntities;

import javax.persistence.*;

import infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "car_models")
@Getter
@Setter
public class CarModelEntity extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private CarBrandEntity brand;

    @Column(length = 50)
    private String generation;
}
