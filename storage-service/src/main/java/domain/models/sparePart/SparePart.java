package domain.models.sparePart;

import domain.exception.DomainValidationException;
import domain.models.car.CarModel;
import domain.models.car.Price;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class SparePart
{
    private String id;
    private SpareType type;
    private String name;
    private String description;
    private Price price;
    private String manufacturer;
    private String partNumber;
    private final Set<CarModel> compatibles;

    public SparePart(String id, SpareType type, String name, String description, Price price, Set<CarModel> compatibles) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.price = price;
        this.compatibles = new HashSet<>(compatibles);
        this.manufacturer = null;
        this.partNumber = null;
        validate();
    }

    public SparePart(String id, SpareType type, String name, String description, Price price,
                     Set<CarModel> compatibles, String manufacturer, String partNumber) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.price = price;
        this.compatibles = new HashSet<>(compatibles);
        this.manufacturer = manufacturer;
        this.partNumber = partNumber;
        validate();
    }

    private void validate() {
        if (type == null) {
            throw new NullPointerException("Type cannot be null");
        }
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        if (price == null) {
            throw new NullPointerException("Price cannot be null");
        }
    }

    public boolean isCompatibleWith(CarModel model) {
        if (model == null) {
            throw new DomainValidationException("Model cannot be null");
        }
        return compatibles.contains(model);
    }

    public Set<CarModel> getCompatibles() {
        return Collections.unmodifiableSet(compatibles);
    }

    public void setCompatibles(Set<CarModel> newCompatibles) {
        this.compatibles.clear();
        if (newCompatibles != null) {
            this.compatibles.addAll(newCompatibles);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private SpareType type;
        private String name;
        private String description = "";
        private Price price;
        private Set<CarModel> compatibles = new HashSet<>();
        private String manufacturer;
        private String partNumber;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder type(SpareType type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description != null ? description : "";
            return this;
        }

        public Builder price(Price price) {
            this.price = price;
            return this;
        }

        public Builder compatibles(Set<CarModel> compatibles) {
            this.compatibles = compatibles != null ? compatibles : new HashSet<>();
            return this;
        }

        public Builder manufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public Builder partNumber(String partNumber) {
            this.partNumber = partNumber;
            return this;
        }

        public SparePart build() {
            if (id == null) {
                id = UUID.randomUUID().toString();
            }
            if (manufacturer != null || partNumber != null) {
                return new SparePart(id, type, name, description, price, compatibles, manufacturer, partNumber);
            }
            return new SparePart(id, type, name, description, price, compatibles);
        }
    }
}