package application.services.carService.client;

import application.dtos.request.carRequest.ApplyConfigurationRequest;
import application.dtos.response.carResponse.CarResponse;
import application.dtos.response.carResponse.componentResponse.CarConfigurationResponse;
import application.mapper.CarMapper;
import application.services.carService.BaseCarService;
import domain.exception.DomainValidationException;
import domain.models.car.Car;
import domain.models.car.CarConfiguration;
import domain.models.car.componentModels.ComponentType;
import domain.repository.carRepository.CarRepository;
import domain.repository.carRepository.ConfigurationRepository;
import infrastructure.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CarClientServiceImpl extends BaseCarService implements CarClientService {

    private final ConfigurationRepository configurationRepository;
    private final CarMapper carMapper;
    private final RestTemplate restTemplate;

    @Value("${app.order-service.url:http://localhost:8081/api}")
    private String orderServiceUrl;

    public CarClientServiceImpl(
            CarRepository carRepository,
            CarMapper carMapper,
            ConfigurationRepository configurationRepository,
            RestTemplate restTemplate) {
        super(carRepository, carMapper);
        this.configurationRepository = configurationRepository;
        this.carMapper = carMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public CarResponse applyConfiguration(ApplyConfigurationRequest request) {
        log.info("Applying configuration to car: {}", request.getCarId());

        Car car = findCarById(request.getCarId());

        CarConfiguration config = configurationRepository.findById(request.getConfigurationId())
                .orElseThrow(() -> new DomainValidationException("Configuration not found"));

//        if (request.getSelectedComponents() != null && !request.getSelectedComponents().isEmpty()) {
//            validateSelectedComponents(config, request.getSelectedComponents());
//        }

        car.applyConfiguration(config);
        Car updated = saveCar(car);

        log.info("Configuration applied successfully to car: {}", request.getCarId());
        return carMapper.toResponse(updated);
    }

//    private void validateSelectedComponents(CarConfiguration config, Map<String, String> selectedComponents) {
//        Map<ComponentType, Component> components = selectedComponents.entrySet().stream()
//                .collect(Collectors.toMap(
//                        entry -> ComponentType.valueOf(entry.getKey()),
//                        entry -> findComponentById(config, entry.getValue())
//                ));
//
//        config.isValidConfiguration(components);
//    }

//    private Component findComponentById(CarConfiguration config, String componentId) {
//        return config.getBaseComponents().values().stream()
//                .filter(c -> c.getId().equals(componentId))
//                .findFirst()
//                .orElseThrow(() -> new DomainValidationException("Component not found: " + componentId));
//    }

    @Override
    @Transactional(readOnly = true)
    public List<CarConfigurationResponse> getConfigurationsForModel(String modelId) {
        log.debug("Getting configurations for model: {}", modelId);
        List<CarConfiguration> configs = configurationRepository.findByModelId(modelId);
        return configs.stream()
                .map(carMapper::toConfigurationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void sendTestDriveRequest(String carId, LocalDateTime requestedTime) {
        log.info("Sending test drive request for car: {}, time: {}", carId, requestedTime);

        Car car = findCarById(carId);

        if (!car.isAvailableForTestDrive()) {
            throw new DomainValidationException("Car is not available for test drive");
        }

        if (requestedTime.isBefore(LocalDateTime.now())) {
            throw new DomainValidationException("Test drive cannot be in the past");
        }

        if (requestedTime.isAfter(LocalDateTime.now().plusMonths(1))) {
            throw new DomainValidationException("Test drive cannot be more than 1 month in advance");
        }

        String url = orderServiceUrl + "/client/test-drives";

        HttpHeaders headers = new HttpHeaders();
        String token = SecurityUtils.getCurrentJwtToken();
        headers.set("Authorization", "Bearer " + token);

        Map<String, Object> requestBody = Map.of(
                "carId", carId,
                "startTime", requestedTime.toString()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info("Test drive request sent successfully for car: {}", carId);
        } catch (Exception e) {
            log.error("Failed to send test drive request for car: {}", carId, e);
            throw new DomainValidationException("Failed to create test drive request: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void makeOrderOnCar(String carId) {
        log.info("Making order on car: {}", carId);

        Car car = findCarById(carId);

        if (!car.isAvailableForPurchase()) {
            throw new DomainValidationException("Car is not available for purchase");
        }

        String url = orderServiceUrl + "/client/orders";

        HttpHeaders headers = new HttpHeaders();
        String token = SecurityUtils.getCurrentJwtToken();
        headers.set("Authorization", "Bearer " + token);

        Map<String, Object> requestBody = Map.of(
                "carId", carId,
                "orderType", "IN_STOCK"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            car.reserve();
            saveCar(car);
            log.info("Order created successfully for car: {}", carId);
        } catch (Exception e) {
            log.error("Failed to create order for car: {}", carId, e);
            throw new DomainValidationException("Failed to create order: " + e.getMessage());
        }
    }
}