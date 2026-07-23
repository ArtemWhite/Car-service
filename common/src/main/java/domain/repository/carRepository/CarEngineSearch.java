package domain.repository.carRepository;

import domain.models.car.Car;

import java.util.List;

public interface CarEngineSearch {
    List<Car> findByEngineFuelType(String fuelType);
    List<Car> findByEnginePowerRange(double minPower, double maxPower);
    List<Car> findByEngineDisplacementRange(double minVolume, double maxVolume);
}