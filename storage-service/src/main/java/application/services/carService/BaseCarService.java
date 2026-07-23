package application.services.carService;

import application.mapper.CarMapper;
import domain.exception.EntityNotFoundException;
import domain.models.car.Car;
import domain.repository.carRepository.CarRepository;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseCarService {

    protected final CarRepository carRepository;
    protected final CarMapper carMapper;

    public BaseCarService(CarRepository carRepository, CarMapper carMapper) {
        this.carRepository = carRepository;
        this.carMapper = carMapper;
    }

    protected Car findCarById(String carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + carId));
    }

    protected Car saveCar(Car car) {
        return carRepository.save(car);
    }
}