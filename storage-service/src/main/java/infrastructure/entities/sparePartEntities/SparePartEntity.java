package infrastructure.entities.sparePartEntities;

import javax.persistence.*;

import domain.models.car.CarModel;
import infrastructure.entities.BaseEntity;
import infrastructure.entities.sparePartEntities.referenceSparePartEntities.SparePartCompatibilityEntity;
import infrastructure.entities.sparePartEntities.referenceSparePartEntities.SparePartTypeEntity;
import infrastructure.mappers.carEntitiesMappers.carReferenceMappers.CarModelEntityMapper;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "spare_parts")
@Where(clause = "removed = false")
@Getter
@Setter
public class SparePartEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private SparePartTypeEntity type;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String manufacturer;

    @Column(name = "part_number", length = 100)
    private String partNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    private String currency = "RUB";

    @Column(name = "stock_quantity")
    private int stockQuantity = 0;

    @Column(name = "section_id", length = 50)
    private String sectionId;

    @Column(length = 100)
    private String location;

    @OneToMany(mappedBy = "sparePart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<SparePartCompatibilityEntity> compatibilities = new ArrayList<>();

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public boolean isLowStock(int threshold) {
        return stockQuantity > 0 && stockQuantity < threshold;
    }

    public boolean isOutOfStock() {
        return stockQuantity == 0;
    }

    public void updateCompatibilities(Set<CarModel> newModels,
                                      CarModelEntityMapper carModelMapper) {
        this.compatibilities.clear();

        for (CarModel model : newModels) {
            SparePartCompatibilityEntity compatibility = new SparePartCompatibilityEntity();
            compatibility.setId(UUID.randomUUID());
            compatibility.setSparePart(this);
            compatibility.setCarModel(carModelMapper.toEntity(model));
            compatibility.setCreatedAt(Instant.now());
            compatibility.setUpdatedAt(Instant.now());
            compatibility.setRemoved(false);
            this.compatibilities.add(compatibility);
        }
    }
}