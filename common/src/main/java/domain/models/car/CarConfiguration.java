package domain.models.car;

import domain.models.car.componentModels.Component;
import domain.models.car.componentModels.ComponentType;
import domain.exception.DomainValidationException;
import domain.exception.IncompatibleComponentException;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public class CarConfiguration
{
    private final String id;
    private final String name;
    private final CarModel model;
    private final Map<ComponentType, Component> baseComponents;
    private final Price basePrice;

    public CarConfiguration(String id, String name, CarModel model,
                            Map<ComponentType, Component> baseComponents,
                            Price basePrice) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.baseComponents = new HashMap<>(baseComponents);
        this.basePrice = basePrice;
    }

    public Price calculateTotalPrice(Map<ComponentType, Component> selectedComponents) {
        Price total = basePrice;

        for (Map.Entry<ComponentType, Component> entry : selectedComponents.entrySet()) {
            Component base = baseComponents.get(entry.getKey());
            Component selected = entry.getValue();

            if (base == null) {
                throw new DomainValidationException("Invalid component type: " + entry.getKey());
            }

            if (!base.equals(selected)) {
                total = total.add(selected.getExtraCharge());
            }
        }

        return total;
    }

    public boolean isValidForModel(CarModel model) {
        return this.model.equals(model);
    }

    public void isValidConfiguration(Map<ComponentType, Component> selectedComponents)
    {
        for (ComponentType type : baseComponents.keySet())
        {
            if (!selectedComponents.containsKey(type))
            {
                throw new IncompatibleComponentException("Missing required component: " + type.getDisplayName());
            }
        }

        for (Component component : selectedComponents.values())
        {
            if (!component.isCompatibleWith(model))
            {
                throw new IncompatibleComponentException("Component " + component.getName() + " is not compatible with " + model.getName());
            }
        }
    }

    public Map<ComponentType, Component> getBaseComponents() {
        return Collections.unmodifiableMap(baseComponents);
    }
}
