package domain.repository.carRepository;

import domain.models.car.Car;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.types.*;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;

import java.util.List;
import java.util.Optional;

public interface CarFilterSearch {
    List<Car> findCarsByFilters(CarBrand brand, CarModel model,
                                CarBody body, CarColor color,
                                DriveType driveType, Price minPrice, Price maxPrice);
    Optional<CarModel> findModelById(String modelId);
    Optional<CarModel> findModelByNameAndBrand(String modelName, CarBrand brand);

    Optional<Engine> findEngineByFuelTypePowerAndDisplacement(EngineFuelType fuelType, double power, double displacement);
    Optional<Transmission> findTransmissionByTypeAndGears(TransmissionType type, int gears);

    Engine saveEngine(Engine engine);
    Transmission saveTransmission(Transmission transmission);
}