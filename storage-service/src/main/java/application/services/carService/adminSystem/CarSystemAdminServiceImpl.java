package application.services.carService.adminSystem;

import application.dtos.request.carRequest.CreateCarRequest;
import application.dtos.request.carRequest.UpdateCarRequest;
import application.dtos.response.carResponse.CarResponse;
import application.mapper.CarMapper;
import application.services.carService.BaseCarService;
import domain.exception.DomainValidationException;
import domain.models.car.Car;
import domain.models.car.CarModel;
import domain.models.car.types.CarBrand;
import domain.models.car.types.CarStatus;
import domain.repository.carRepository.CarRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CarSystemAdminServiceImpl extends BaseCarService implements CarSystemAdminService {

    public CarSystemAdminServiceImpl(CarRepository carRepository, CarMapper carMapper) {
        super(carRepository, carMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarResponse> getAllCars() {
        log.debug("Getting all cars");
        return carMapper.toResponseList(carRepository.findAll());
    }

    @Override
    @Transactional
    public CarResponse createCar(CreateCarRequest request) {
        log.info("Creating new car: brand={}, model={}", request.getBrand(), request.getModel());

        CarBrand brand = CarBrand.valueOf(request.getBrand());

        Optional<CarModel> existingModel = carRepository.findModelByNameAndBrand(
                request.getModel(),
                brand
        );

        Car car = carMapper.toDomain(request, existingModel.orElse(null));
        car.markAsAvailable();

        Car saved = saveCar(car);
        log.info("Car created successfully with id: {}", saved.getCarId());

        return carMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CarResponse updateCar(String carId, UpdateCarRequest request) {
        log.info("Updating car: {}, price={}, status={}", carId, request.getPrice(), request.getStatus());

        Car car = findCarById(carId);

        if (car.getCarStatus() == CarStatus.SOLD) {
            throw new DomainValidationException("Cannot update sold car");
        }

        if (request.getPrice() != null) {
            car.setPrice(domain.models.car.Price.of(request.getPrice(), "RUB"));
        }

        if (request.getStatus() != null) {
            carMapper.updateCarStatus(car, request.getStatus());
        }

        Car updated = saveCar(car);
        log.info("Car updated successfully: {}", carId);

        return carMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCar(String carId, String reason) {
        log.info("Deleting car: {}, reason: {}", carId, reason);

        Car car = findCarById(carId);

        if (car.getCarStatus() != CarStatus.UNAVAILABLE) {
            throw new DomainValidationException("Can only delete unavailable cars");
        }

        carRepository.delete(carId);
        log.info("Car deleted successfully: {}", carId);
    }

    @Override
    @Transactional
    public CarResponse changeCarStatus(String carId, String status) {
        log.info("Changing car status: {}, new status: {}", carId, status);

        Car car = findCarById(carId);
        carMapper.updateCarStatus(car, status);
        Car updated = saveCar(car);

        log.info("Car status changed successfully: {} -> {}", carId, updated.getCarStatus());
        return carMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public CarResponse getCarById(String carId) {
        log.debug("Getting car by id: {}", carId);
        Car car = findCarById(carId);
        return carMapper.toResponse(car);
    }
}