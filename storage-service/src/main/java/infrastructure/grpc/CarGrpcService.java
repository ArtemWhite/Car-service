package infrastructure.grpc;

import com.dealershipOrder.grpc.*;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CarGrpcService extends CarServiceGrpc.CarServiceImplBase {

    private final CarServiceDomain carService;

    @Override
    public void getAvailableCars(EmptyRequest request,
                                 StreamObserver<CarListResponse> responseObserver) {
        log.info("gRPC request: getAvailableCars()");

        try {
            List<Car> availableCars = carService.getAvailableCars();

            CarListResponse.Builder responseBuilder;
            responseBuilder = CarListResponse.newBuilder();

            for (Car car : availableCars) {
                responseBuilder.addCars(toProtoCar(car));
            }

            responseBuilder.setTotalCount(availableCars.size());

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

            log.info("gRPC response: {} cars available", availableCars.size());

        } catch (Exception e) {
            log.error("Error in getAvailableCars gRPC method", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void getCarById(CarIdRequest request,
                           StreamObserver<CarResponse> responseObserver) {
        String carId = request.getId();
        log.info("gRPC request: getCarById({})", carId);

        try {
            Car car = carService.getCarById(carId);

            if (car == null) {
                responseObserver.onError(
                        Status.NOT_FOUND.withDescription("Car not found: " + carId).asRuntimeException()
                );
                return;
            }

            responseObserver.onNext(toProtoCar(car));
            responseObserver.onCompleted();

            log.info("gRPC response: car {} found", carId);

        } catch (Exception e) {
            log.error("Error in getCarById gRPC method for id: {}", carId, e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    private CarResponse toProtoCar(Car car) {
        CarResponse.Builder builder = CarResponse.newBuilder();

        builder.setId(car.getCarId());
        builder.setBrand(car.getBrand());
        builder.setModel(car.getModel());
        builder.setBodyType(car.getBodyType());
        builder.setColor(car.getColor());
        builder.setDriveType(car.getDriveType());
        builder.setPrice(car.getPrice());
        builder.setCurrency(car.getCurrency());
        builder.setStatus(car.getStatus());
        builder.setAvailableForPurchase(car.isAvailableForPurchase());
        builder.setConfigurationId(car.getConfigurationId());
        builder.setConfigurationName(car.getConfigurationName());
        builder.setCarInfo(car.getCarInfo());

        EngineInfo.Builder engineBuilder = EngineInfo.newBuilder();
        engineBuilder.setFuelType(car.getEngineFuelType());
        engineBuilder.setPower(car.getEnginePower());
        engineBuilder.setDisplacement(car.getEngineDisplacement());
        engineBuilder.setDescription(car.getEngineDescription());
        builder.setEngine(engineBuilder.build());

        TransmissionInfo.Builder transBuilder =
                TransmissionInfo.newBuilder();
        transBuilder.setType(car.getTransmissionType());
        transBuilder.setGears(car.getGears());
        transBuilder.setDescription(car.getTransmissionDescription());
        builder.setTransmission(transBuilder.build());

        return builder.build();
    }
}

interface CarServiceDomain {
    List<Car> getAvailableCars();
    Car getCarById(String id);
}

class Car {
    private String carId;
    private String brand;
    private String model;
    private String bodyType;
    private String color;
    private String driveType;
    private double price;
    private String currency;
    private String status;
    private boolean availableForPurchase;
    private String configurationId;
    private String configurationName;
    private String carInfo;
    private String engineFuelType;
    private double enginePower;
    private double engineDisplacement;
    private String engineDescription;
    private String transmissionType;
    private int gears;
    private String transmissionDescription;

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getDriveType() { return driveType; }
    public void setDriveType(String driveType) { this.driveType = driveType; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isAvailableForPurchase() { return availableForPurchase; }
    public void setAvailableForPurchase(boolean availableForPurchase) { this.availableForPurchase = availableForPurchase; }
    public String getConfigurationId() { return configurationId; }
    public void setConfigurationId(String configurationId) { this.configurationId = configurationId; }
    public String getConfigurationName() { return configurationName; }
    public void setConfigurationName(String configurationName) { this.configurationName = configurationName; }
    public String getCarInfo() { return carInfo; }
    public void setCarInfo(String carInfo) { this.carInfo = carInfo; }
    public String getEngineFuelType() { return engineFuelType; }
    public void setEngineFuelType(String engineFuelType) { this.engineFuelType = engineFuelType; }
    public double getEnginePower() { return enginePower; }
    public void setEnginePower(double enginePower) { this.enginePower = enginePower; }
    public double getEngineDisplacement() { return engineDisplacement; }
    public void setEngineDisplacement(double engineDisplacement) { this.engineDisplacement = engineDisplacement; }
    public String getEngineDescription() { return engineDescription; }
    public void setEngineDescription(String engineDescription) { this.engineDescription = engineDescription; }
    public String getTransmissionType() { return transmissionType; }
    public void setTransmissionType(String transmissionType) { this.transmissionType = transmissionType; }
    public int getGears() { return gears; }
    public void setGears(int gears) { this.gears = gears; }
    public String getTransmissionDescription() { return transmissionDescription; }
    public void setTransmissionDescription(String transmissionDescription) { this.transmissionDescription = transmissionDescription; }
}