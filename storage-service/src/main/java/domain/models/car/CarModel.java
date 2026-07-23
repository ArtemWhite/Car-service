package domain.models.car;

import domain.models.car.types.CarBrand;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
public class CarModel
{
    private final String id;
    private final String name;
    private final CarBrand carBrand;
    private final String generation;

    public CarModel(String id, String name, CarBrand carBrand, String generation)
    {
        this.id = id;
        this.name = name;
        this.carBrand = carBrand;
        this.generation = generation;
    }

    public String getFullName()
    {
        return generation == null ? carBrand.getDisplayName() + " " +
                name : carBrand.getDisplayName() + " " + " (" + generation + " )";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarModel carModel = (CarModel) o;
        return Objects.equals(name, carModel.name) && carBrand == carModel.carBrand && Objects.equals(generation, carModel.generation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, carBrand, generation);
    }
}
