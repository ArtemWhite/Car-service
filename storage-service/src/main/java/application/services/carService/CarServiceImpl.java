package application.services.carService;

import application.dtos.request.carRequest.CarFilterRequest;
import application.dtos.response.carResponse.CarResponse;
import application.mapper.CarMapper;
import domain.models.car.Car;
import domain.repository.carRepository.CarRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CarServiceImpl extends BaseCarService implements CarService {

    public CarServiceImpl(CarRepository carRepository, CarMapper carMapper) {
        super(carRepository, carMapper);
    }

    @Override
    public CarResponse getCarById(String id) {
        log.debug("Getting car by id: {}", id);
        Car car = findCarById(id);
        return carMapper.toResponse(car);
    }

    @Override
    public List<CarResponse> getAvailableCars() {
        log.debug("Getting all available cars");
        List<Car> cars = carRepository.findAvailableCars();
        return carMapper.toResponseList(cars);
    }

    @Override
    public List<CarResponse> getCarsWithFilters(CarFilterRequest filter) {
        log.debug("Getting cars with filters: {}", filter);
        CarMapper.CarFilter domainFilter = carMapper.toDomainFilter(filter);
        List<Car> cars = carRepository.findCarsByFilters(
                domainFilter.getBrand(),
                domainFilter.getModel(),
                domainFilter.getBodyType(),
                domainFilter.getColor(),
                domainFilter.getDriveType(),
                domainFilter.getMinPrice(),
                domainFilter.getMaxPrice()
        );
        return carMapper.toResponseList(cars);
    }
}