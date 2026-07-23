package application.mapper;

import application.dtos.request.carRequest.CarFilterRequest;
import application.dtos.request.carRequest.CreateCarRequest;
import application.dtos.request.carRequest.UpdateCarRequest;
import application.dtos.response.carResponse.CarResponse;
import application.dtos.response.carResponse.componentResponse.CarConfigurationResponse;
import application.dtos.response.carResponse.componentResponse.ComponentResponse;
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
import domain.repository.carRepository.CarRepository;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class CarMapper {

    private final CarRepository carRepository;

    public CarMapper(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    private Engine findOrCreateEngine(CreateCarRequest request) {
        EngineFuelType fuelType = EngineFuelType.valueOf(request.getEngineFuelType());
        double power = request.getEnginePower();
        double displacement = request.getEngineDisplacement();

        return carRepository.findEngineByFuelTypePowerAndDisplacement(fuelType, power, displacement)
                .orElseGet(() -> {
                    Engine newEngine = new Engine(
                            UUID.randomUUID().toString(),
                            fuelType,
                            EngineDisplacement.of(displacement),
                            EnginePower.of(power)
                    );
                    return carRepository.saveEngine(newEngine);
                });
    }

    private Transmission findOrCreateTransmission(CreateCarRequest request) {
        TransmissionType type = TransmissionType.valueOf(request.getTransmissionType());
        int gears = request.getTransmissionGears();

        return carRepository.findTransmissionByTypeAndGears(type, gears)
                .orElseGet(() -> {
                    Transmission newTransmission = new Transmission(type, gears);
                    return carRepository.saveTransmission(newTransmission);
                });
    }

    public Car toDomain(CreateCarRequest request, CarModel existingModel) {
        Engine engine = findOrCreateEngine(request);
        Transmission transmission = findOrCreateTransmission(request);

        CarModel carModel;
        if (existingModel != null) {
            carModel = existingModel;
        } else {
            carModel = new CarModel(
                    UUID.randomUUID().toString(),
                    request.getModel(),
                    CarBrand.valueOf(request.getBrand()),
                    null
            );
        }

        Price price = Price.of(request.getPrice(), "RUB");

        return new Car(
                UUID.randomUUID().toString(),
                CarBrand.valueOf(request.getBrand()),
                carModel,
                CarBody.valueOf(request.getBodyType()),
                CarColor.valueOf(request.getColor()),
                DriveType.valueOf(request.getDriveType()),
                engine,
                transmission,
                price
        );
    }

    public void updateDomain(Car car, UpdateCarRequest request) {
        if (request.getPrice() != null) {
            car.setPrice(Price.of(request.getPrice(), "RUB"));
        }

        if (request.getStatus() != null) {
            updateCarStatus(car, request.getStatus());
        }
    }

    public void updateCarStatus(Car car, String status) {
        switch (status) {
            case "AVAILABLE":
                car.markAsAvailable();
                break;
            case "SOLD":
                car.markAsSold();
                break;
            case "TEST_DRIVE_AVAILABLE":
                car.addToTestDriveFleet();
                break;
            case "IN_SERVICE":
                car.markAsInService();
                break;
            case "RESERVED":
                car.reserve();
                break;
            case "UNAVAILABLE":
                car.markAsUnavailable();
                break;
        }
    }

    public CarFilter toDomainFilter(CarFilterRequest request) {
        CarModel model = null;
        if (request.getModel() != null && request.getBrand() != null) {
            model = new CarModel(
                    UUID.randomUUID().toString(),
                    request.getModel(),
                    CarBrand.valueOf(request.getBrand()),
                    null
            );
        }

        return new CarFilter(
                request.getBrand() != null ? CarBrand.valueOf(request.getBrand()) : null,
                model,
                request.getBodyType() != null ? CarBody.valueOf(request.getBodyType()) : null,
                request.getColor() != null ? CarColor.valueOf(request.getColor()) : null,
                request.getDriveType() != null ? DriveType.valueOf(request.getDriveType()) : null,
                request.getMinPrice() != null ? Price.of(request.getMinPrice(), "RUB") : null,
                request.getMaxPrice() != null ? Price.of(request.getMaxPrice(), "RUB") : null
        );
    }

    public CarResponse toResponse(Car car) {
        CarResponse response = new CarResponse();

        response.setId(car.getCarId());
        response.setBrand(car.getBrand().name());
        response.setBrandDisplayName(car.getBrand().getDisplayName());

        if (car.getModel() != null) {
            response.setModel(car.getModel().getName());
            response.setModelFullName(car.getModel().getFullName());
        }

        response.setBodyType(car.getBody().name());
        response.setBodyDisplayName(car.getBody().getDisplayName());
        response.setColor(car.getColor().name());
        response.setColorDisplayName(car.getColor().getDisplayName());
        response.setColorPrice(car.getColor().getColorPrice());
        response.setDriveType(car.getDriveType().name());
        response.setDriveDisplayName(car.getDriveType().getDisplayName());

        response.setEngineFuelType(car.getEngine().getEngineFuelType().name());
        response.setEngineFuelDisplayName(car.getEngine().getEngineFuelType().getDisplayName());
        response.setEnginePower(car.getEngine().getEnginePower().getHorsePower());
        response.setEngineDisplacement(car.getEngine().getEngineDisplacement().getLiters());
        response.setEngineDescription(car.getEngine().getDescription());

        response.setTransmissionType(car.getTransmission().getTransmissionType().name());
        response.setTransmissionDisplayName(car.getTransmission().getTransmissionType().getDisplayName());
        response.setTransmissionGears(car.getTransmission().getGears());
        response.setTransmissionDescription(car.getTransmission().getFullName());

        response.setPrice(car.getPrice().getAmount().doubleValue());
        response.setPriceFormatted(formatPrice(car.getPrice()));
        response.setStatus(car.getCarStatus().name());
        response.setStatusDisplayName(car.getCarStatus().getDisplayName());

        if (car.getConfiguration() != null) {
            response.setConfigurationId(car.getConfiguration().getId());
            response.setConfigurationName(car.getConfiguration().getName());
        }

        response.setAvailableForPurchase(car.getCarStatus() == CarStatus.AVAILABLE);
        response.setAvailableForTestDrive(car.getCarStatus() == CarStatus.TEST_DRIVE_AVAILABLE);
        response.setInStock(car.getCarStatus() == CarStatus.IN_STOCK);

        return response;
    }

    public List<CarResponse> toResponseList(List<Car> cars) {
        return cars.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CarConfigurationResponse toConfigurationResponse(CarConfiguration config) {
        CarConfigurationResponse response = new CarConfigurationResponse();

        response.setId(config.getId());
        response.setName(config.getName());
        response.setModelName(config.getModel().getName());
        response.setBasePrice(formatPrice(config.getBasePrice()));

        List<ComponentResponse> baseComponents = config.getBaseComponents().entrySet().stream()
                .map(entry -> {
                    ComponentResponse comp = new ComponentResponse();
                    comp.setType(entry.getKey().name());
                    comp.setTypeDisplayName(entry.getKey().getDisplayName());
                    comp.setId(entry.getValue().getId());
                    comp.setName(entry.getValue().getName());
                    comp.setDescription(entry.getValue().getDescription());
                    comp.setPrice(formatPrice(entry.getValue().getExtraCharge()));
                    comp.setPriceValue(entry.getValue().getExtraCharge().getAmount().doubleValue());
                    comp.setSelected(true);
                    return comp;
                })
                .collect(Collectors.toList());

        response.setBaseComponents(baseComponents);

        return response;
    }

    public CarConfigurationResponse toConfigurationResponse(
            CarConfiguration config,
            java.util.Map<ComponentType, Component> selectedComponents) {

        CarConfigurationResponse response = toConfigurationResponse(config);

        Price totalPrice = config.calculateTotalPrice(selectedComponents);
        response.setTotalPrice(formatPrice(totalPrice));
        response.setTotalPriceValue(totalPrice.getAmount().doubleValue());

        response.getBaseComponents().forEach(comp -> {
            comp.setSelected(selectedComponents.containsKey(
                    ComponentType.valueOf(comp.getType())
            ));
        });

        return response;
    }

    public ComponentResponse toComponentResponse(Component component, boolean selected) {
        ComponentResponse response = new ComponentResponse();

        response.setId(component.getId());
        response.setType(component.getType().name());
        response.setTypeDisplayName(component.getType().getDisplayName());
        response.setName(component.getName());
        response.setDescription(component.getDescription());
        response.setPrice(formatPrice(component.getExtraCharge()));
        response.setPriceValue(component.getExtraCharge().getAmount().doubleValue());
        response.setSelected(selected);

        return response;
    }

    public String formatPrice(Price price) {
        return String.format("%,.0f %s",
                price.getAmount(),
                price.getCurrency().getSymbol()
        ).replace(',', ' ');
    }

    public Car toDomain(CreateCarRequest request) {
        return toDomain(request, null);
    }

    @Getter
    public static class CarFilter {
        private final CarBrand brand;
        private final CarModel model;
        private final CarBody bodyType;
        private final CarColor color;
        private final DriveType driveType;
        private final Price minPrice;
        private final Price maxPrice;

        public CarFilter(CarBrand brand, CarModel model, CarBody bodyType,
                         CarColor color, DriveType driveType, Price minPrice, Price maxPrice) {
            this.brand = brand;
            this.model = model;
            this.bodyType = bodyType;
            this.color = color;
            this.driveType = driveType;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }
}