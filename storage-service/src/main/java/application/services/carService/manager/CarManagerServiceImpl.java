package application.services.carService.manager;

import application.dtos.response.carResponse.CarResponse;
import application.dtos.response.carResponse.componentResponse.CarConfigurationResponse;
import application.mapper.CarMapper;
import application.services.carService.BaseCarService;
import domain.exception.DomainValidationException;
import domain.models.car.Car;
import domain.models.car.types.CarStatus;
import domain.repository.carRepository.CarRepository;
import domain.repository.carRepository.ConfigurationRepository;
import infrastructure.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class CarManagerServiceImpl extends BaseCarService implements CarManagerService {

    private final CarMapper carMapper;
    private final ConfigurationRepository configurationRepository;
    private final RestTemplate restTemplate;

    @Value("${app.order-service.url:http://localhost:8081/api}")
    private String orderServiceUrl;

    public CarManagerServiceImpl(
            CarRepository carRepository,
            CarMapper carMapper,
            ConfigurationRepository configurationRepository,
            RestTemplate restTemplate) {
        super(carRepository, carMapper);
        this.carMapper = carMapper;
        this.configurationRepository = configurationRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public void addCarToTestDriveFleet(String carId) {
        log.info("Adding car to test drive fleet: {}", carId);

        Car car = findCarById(carId);

        if (car.getCarStatus() != CarStatus.AVAILABLE && car.getCarStatus() != CarStatus.IN_STOCK) {
            throw new DomainValidationException("Car cannot be used for test drives");
        }

        car.addToTestDriveFleet();
        saveCar(car);
        log.info("Car added to test drive fleet: {}", carId);
    }

    @Override
    @Transactional
    public void removeCarFromTestDriveFleet(String carId) {
        log.info("Removing car from test drive fleet: {}", carId);

        Car car = findCarById(carId);
        car.markAsAvailable();
        saveCar(car);

        log.info("Car removed from test drive fleet: {}", carId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarResponse> getTestDriveFleet() {
        log.debug("Getting test drive fleet");
        return carMapper.toResponseList(carRepository.findCarsForTestDrive());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarResponse> getOrdersOnAvailableCars() {
        log.debug("Getting orders on available cars from order-service");

        String url = orderServiceUrl + "/manager/orders/available-cars";
        HttpHeaders headers = new HttpHeaders();
        String token = SecurityUtils.getCurrentJwtToken();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<CarResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<CarResponse>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get orders on available cars", e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarConfigurationResponse> getOrdersOnConfigurationCars() {
        log.debug("Getting orders on configuration cars from order-service");

        String url = orderServiceUrl + "/manager/orders/configuration-cars";
        HttpHeaders headers = new HttpHeaders();
        String token = SecurityUtils.getCurrentJwtToken();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<CarConfigurationResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<CarConfigurationResponse>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get orders on configuration cars", e);
            return List.of();
        }
    }
}