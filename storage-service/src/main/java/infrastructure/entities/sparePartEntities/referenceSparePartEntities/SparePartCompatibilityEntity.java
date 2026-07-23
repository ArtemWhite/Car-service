package infrastructure.entities.sparePartEntities.referenceSparePartEntities;

import javax.persistence.*;

import infrastructure.entities.BaseEntity;
import infrastructure.entities.carEntities.referenceCarEntities.CarModelEntity;
import infrastructure.entities.sparePartEntities.SparePartEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "spare_part_compatibilities")
@Getter
@Setter
public class SparePartCompatibilityEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SparePartEntity sparePart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_model_id", nullable = false)
    private CarModelEntity carModel;
}