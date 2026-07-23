package domain.repository.carRepository;

import domain.models.car.Car;
import domain.models.car.types.CarStatus;

import java.util.List;

public interface CarStatusSearch {
    List<Car> findByStatus(CarStatus status);
    List<Car> findAvailableCars();
    List<Car> findCarsForTestDrive();
    long countByStatus(CarStatus status);
    long countAvailableCars();
}