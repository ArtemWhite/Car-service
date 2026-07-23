package domainTest.car.configurationTest;

import domain.models.car.Car;
import domain.models.car.CarConfiguration;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.componentModels.Component;
import domain.models.car.componentModels.ComponentType;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CarTest
{
    @Test
    public void shouldApplyConfiguration() {

        CarModel carModel1 = new CarModel("23", "X5", CarBrand.BMW, "32");
        CarModel carModel2 = new CarModel("536", "X6", CarBrand.BMW, "2");

        Car car = new Car("312", CarBrand.BMW,
                carModel1,
                CarBody.UNIVERSAL, CarColor.RED, DriveType.REAR,
                new Engine("des", EngineFuelType.DIESEL, new EngineDisplacement(8.0), new EnginePower(7.4)),
                new Transmission(TransmissionType.AUTOMATIC, 8),
                new Price(BigDecimal.valueOf(45003213), Currency.getInstance("RUB"), false));

        car.markAsAvailable();

        Set<CarModel> carModelSet = new HashSet<>();
        carModelSet.add(carModel1);
        carModelSet.add(carModel2);


        Map<ComponentType, Component> baseComponents = new HashMap<>();
        Component component1 = new Component("32",
                ComponentType.ELECTRONICS,
                "Магнитофон", "для музыки",
                new Price(BigDecimal.valueOf(2500), Currency.getInstance("RUB"), false),
                carModelSet);
        Component component2 = new Component("32",
                ComponentType.INTERIOR,
                "Сиденье", "для кайфа",
                new Price(BigDecimal.valueOf(25000), Currency.getInstance("RUB"), false),
                carModelSet);

        baseComponents.put(ComponentType.ELECTRONICS, component1);
        baseComponents.put(ComponentType.INTERIOR, component2);

        Price configPrice = new Price(BigDecimal.valueOf(3999), Currency.getInstance("RUB"), false);

        CarConfiguration carConfiguration = new CarConfiguration("frsd", "For Sport",
                carModel1,
                baseComponents,
                configPrice);

        car.applyConfiguration(carConfiguration);

        assertEquals(car.getCarConfiguration(), carConfiguration);
    }

}
