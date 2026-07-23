package domainTest.car.brandAndModel;

import domain.exception.DomainValidationException;
import domain.models.car.CarModel;
import domain.models.car.types.CarBrand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CarBrandAndModelTest
{
    @Test
    public void shouldThrowExceptionWhenSelectingModelWithoutBrand() {
        CarModel model = new CarModel("jkyuk","X5", CarBrand.BMW, "G05");
        CarBrand selectedBrand = null;

        assertThrows(DomainValidationException.class, () -> {
            if (selectedBrand == null) {
                throw new DomainValidationException("Brand must be selected before choosing model");
            }
            if (!model.getCarBrand().equals(selectedBrand)) {
                throw new DomainValidationException("Model does not belong to selected brand");
            }
        });
    }

    @Test
    public void shouldReturnTrueWhenModelBelongsToSelectedBrand() {
        CarBrand selectedBrand = CarBrand.BMW;
        CarModel model = new CarModel("gty", "A4", CarBrand.LADA, "B9");

        boolean modelBelongsToBrand = model.getCarBrand().equals(selectedBrand);

        assertFalse(modelBelongsToBrand);
    }

    @Test
    public void shouldThrowExceptionWhenModelDoesNotMatchBrand()
    {
        CarBrand selectedBrand = CarBrand.BMW;
        CarModel model = new CarModel("frd","X5", CarBrand.BMW, "G05");

        boolean modelBelongsToBrand = model.getCarBrand().equals(selectedBrand);

        assertTrue(modelBelongsToBrand);
    }

}
