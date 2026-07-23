package infrastructure.adapters.carAdapters.carRepositoriesImpls;

import domain.models.car.Car;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.*;
import domain.repository.carRepository.CarRepository;
import infrastructure.adapters.carAdapters.carReferencesAdapters.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CarRepositoryImpl implements CarRepository {

    private final CarBaseRepositoryAdapter baseAdapter;
    private final CarCharacteristicAdapter characteristicAdapter;
    private final CarStatusAdapter statusAdapter;
    private final CarFilterAdapter filterAdapter;
    private final CarEngineAdapter engineAdapter;

    @Override
    public Car save(Car car) {
        return baseAdapter.save(car);
    }

    @Override
    public Optional<Car> findById(String id) {
        return baseAdapter.findById(id);
    }

    @Override
    public List<Car> findAll() {
        return baseAdapter.findAll();
    }

    @Override
    public void delete(String id) {
        baseAdapter.delete(id);
    }

    @Override
    public boolean existsById(String id) {
        return baseAdapter.existsById(id);
    }

    @Override
    public List<Car> findByBrand(CarBrand brand) {
        return characteristicAdapter.findByBrand(brand);
    }

    @Override
    public List<Car> findByModel(CarModel model) {
        return characteristicAdapter.findByModel(model);
    }

    @Override
    public List<Car> findByPriceRange(Price minPrice, Price maxPrice) {
        return characteristicAdapter.findByPriceRange(minPrice, maxPrice);
    }

    @Override
    public List<Car> findByDriveType(String driveType) {
        return characteristicAdapter.findByDriveType(driveType);
    }

    @Override
    public List<Car> findByColor(String color) {
        return characteristicAdapter.findByColor(color);
    }

    @Override
    public List<Car> findByBody(String body) {
        return characteristicAdapter.findByBody(body);
    }

    @Override
    public List<Car> findByBrandAndModel(String brand, String model) {
        return characteristicAdapter.findByBrandAndModel(brand, model);
    }

    @Override
    public List<Object[]> countCarsByBrand() {
        return characteristicAdapter.countCarsByBrand();
    }

    @Override
    public List<Car> findByStatus(CarStatus status) {
        return statusAdapter.findByStatus(status);
    }

    @Override
    public List<Car> findAvailableCars() {
        return statusAdapter.findAvailableCars();
    }

    @Override
    public List<Car> findCarsForTestDrive() {
        return statusAdapter.findCarsForTestDrive();
    }

    @Override
    public long countByStatus(CarStatus status) {
        return statusAdapter.countByStatus(status);
    }

    @Override
    public long countAvailableCars() {
        return statusAdapter.countAvailableCars();
    }

    @Override
    public List<Car> findCarsByFilters(CarBrand brand, CarModel model,
                                       CarBody body, CarColor color,
                                       DriveType driveType, Price minPrice, Price maxPrice) {
        return filterAdapter.findCarsByFilters(brand, model, body, color, driveType, minPrice, maxPrice);
    }

    @Override
    public Optional<CarModel> findModelById(String modelId) {
        return filterAdapter.findModelById(modelId);
    }

    @Override
    public Optional<CarModel> findModelByNameAndBrand(String modelName, CarBrand brand) {
        return filterAdapter.findModelByNameAndBrand(modelName, brand);
    }

    @Override
    public Optional<Engine> findEngineByFuelTypePowerAndDisplacement(EngineFuelType fuelType, double power, double displacement) {
        return filterAdapter.findEngineByFuelTypePowerAndDisplacement(fuelType, power, displacement);
    }

    @Override
    public Optional<Transmission> findTransmissionByTypeAndGears(TransmissionType type, int gears) {
        return filterAdapter.findTransmissionByTypeAndGears(type, gears);
    }

    @Override
    public Engine saveEngine(Engine engine) {
        return filterAdapter.saveEngine(engine);
    }

    @Override
    public Transmission saveTransmission(Transmission transmission) {
        return filterAdapter.saveTransmission(transmission);
    }

    @Override
    public List<Car> findByEngineFuelType(String fuelType) {
        return engineAdapter.findByEngineFuelType(fuelType);
    }

    @Override
    public List<Car> findByEnginePowerRange(double minPower, double maxPower) {
        return engineAdapter.findByEnginePowerRange(minPower, maxPower);
    }

    @Override
    public List<Car> findByEngineDisplacementRange(double minVolume, double maxVolume) {
        return engineAdapter.findByEngineDisplacementRange(minVolume, maxVolume);
    }
}