package infrastructure.entities.carEntities.technicalCarEntities.transmissionEntity;

import org.hibernate.annotations.Where;

import javax.persistence.*;

import infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transmissions")
@Where(clause = "removed = false")
@Getter
@Setter
public class TransmissionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private TransmissionTypeEntity type;

    private int gears;

    @Column(name = "full_name", length = 100)
    private String fullName;
}