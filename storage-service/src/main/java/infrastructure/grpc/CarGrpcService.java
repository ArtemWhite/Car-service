package infrastructure.grpc;

import application.dtos.response.carResponse.CarResponse;
import application.services.carService.CarService;
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

    private final CarService carService;

    @Override
    public void getAvailableCars(EmptyRequest request,
                                 StreamObserver<CarListResponse> responseObserver) {
        log.info("gRPC request: getAvailableCars()");

        try {
            List<CarResponse> availableCars = carService.getAvailableCars();

            CarListResponse.Builder responseBuilder = CarListResponse.newBuilder();

            for (CarResponse car : availableCars) {
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
                           StreamObserver<com.dealershipOrder.grpc.CarResponse> responseObserver) {
        String carId = request.getId();
        log.info("gRPC request: getCarById({})", carId);

        try {
            CarResponse car = carService.getCarById(carId);

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

    private com.dealershipOrder.grpc.CarResponse toProtoCar(CarResponse car) {
        com.dealershipOrder.grpc.CarResponse.Builder builder =
                com.dealershipOrder.grpc.CarResponse.newBuilder();

        builder.setId(car.getId());
        builder.setBrand(car.getBrandDisplayName() != null ? car.getBrandDisplayName() : car.getBrand());
        builder.setModel(car.getModelFullName() != null ? car.getModelFullName() : car.getModel());
        builder.setBodyType(car.getBodyDisplayName() != null ? car.getBodyDisplayName() : car.getBodyType());
        builder.setColor(car.getColorDisplayName() != null ? car.getColorDisplayName() : car.getColor());
        builder.setDriveType(car.getDriveDisplayName() != null ? car.getDriveDisplayName() : car.getDriveType());
        builder.setPrice(car.getPrice() != null ? car.getPrice() : 0.0);
        builder.setCurrency(car.getCurrency() != null ? car.getCurrency() : "RUB");
        builder.setStatus(car.getStatusDisplayName() != null ? car.getStatusDisplayName() : car.getStatus());
        builder.setAvailableForPurchase(car.isAvailableForPurchase());
        builder.setConfigurationId(car.getConfigurationId() != null ? car.getConfigurationId() : "");
        builder.setConfigurationName(car.getConfigurationName() != null ? car.getConfigurationName() : "");
        builder.setCarInfo(car.getCarInfo() != null ? car.getCarInfo() : "");

        EngineInfo.Builder engineBuilder = EngineInfo.newBuilder();
        engineBuilder.setFuelType(car.getEngineFuelDisplayName() != null ? car.getEngineFuelDisplayName() : car.getEngineFuelType());
        engineBuilder.setPower(car.getEnginePower() != null ? car.getEnginePower() : 0.0);
        engineBuilder.setDisplacement(car.getEngineDisplacement() != null ? car.getEngineDisplacement() : 0.0);
        engineBuilder.setDescription(car.getEngineDescription() != null ? car.getEngineDescription() : "");
        builder.setEngine(engineBuilder.build());

        TransmissionInfo.Builder transBuilder = TransmissionInfo.newBuilder();
        transBuilder.setType(car.getTransmissionDisplayName() != null ? car.getTransmissionDisplayName() : car.getTransmissionType());
        transBuilder.setGears(car.getTransmissionGears() != null ? car.getTransmissionGears() : 0);
        transBuilder.setDescription(car.getTransmissionDescription() != null ? car.getTransmissionDescription() : "");
        builder.setTransmission(transBuilder.build());

        return builder.build();
    }
}
