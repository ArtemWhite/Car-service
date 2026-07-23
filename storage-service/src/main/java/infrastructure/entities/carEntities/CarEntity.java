package infrastructure.entities.carEntities;

import javax.persistence.*;

import infrastructure.entities.BaseEntity;
import infrastructure.entities.carEntities.configurationCarEntities.CarConfigurationEntity;
import infrastructure.entities.carEntities.referenceCarEntities.*;
import infrastructure.entities.carEntities.technicalCarEntities.engineEntities.EngineEntity;
import infrastructure.entities.carEntities.technicalCarEntities.transmissionEntity.TransmissionEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Entity
@Table(name = "cars")
@Where(clause = "removed = false")
@Getter
@Setter
public class CarEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private CarBrandEntity brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private CarModelEntity model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_id")
    private CarBodyEntity body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private CarColorEntity color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_type_id")
    private DriveTypeEntity driveType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engine_id", nullable = false)
    private EngineEntity engine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transmission_id", nullable = false)
    private TransmissionEntity transmission;

    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private CarStatusEntity status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id")
    private CarConfigurationEntity configuration;

    @Column(name = "car_info", length = 500)
    private String carInfo;
}