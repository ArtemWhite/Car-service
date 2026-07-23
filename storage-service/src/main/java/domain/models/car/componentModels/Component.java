package domain.models.car.componentModels;

import domain.models.car.CarModel;
import domain.models.car.Price;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Component
{
    @Getter
    private final String id;
    @Getter
    private final ComponentType type;
    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final Price extraCharge;
    private final Set<CarModel> compatibleModels;

    public boolean isCompatibleWith(CarModel model)
    {
        return compatibleModels.contains(model);
    }

    public Component(String id, ComponentType type, String name, String description,
                     Price extraCharge,
                     Set<CarModel> compatibleModels)
    {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.extraCharge = extraCharge;
        this.compatibleModels = new HashSet<>(compatibleModels);
    }

    public Set<CarModel> getCompatibleModels() {
        return Collections.unmodifiableSet(compatibleModels);
    }

}
