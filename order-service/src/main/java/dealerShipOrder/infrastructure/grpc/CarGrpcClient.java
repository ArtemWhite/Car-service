package dealerShipOrder.infrastructure.grpc;

import com.dealershipOrder.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CarGrpcClient {

    private final ManagedChannel managedChannel;
    private CarServiceGrpc.CarServiceBlockingStub stub;

    @Value("${grpc.client.storage-service.timeout-ms:5000}")
    private long timeoutMs;

    public CarGrpcClient(ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    @PostConstruct
    public void init() {
        this.stub = CarServiceGrpc.newBlockingStub(managedChannel)
                .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS);
        log.info("CarGrpcClient initialized with timeout: {} ms", timeoutMs);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down gRPC channel");
        managedChannel.shutdown();
    }

    public List<CarResponse> getAvailableCars() {
        log.info("gRPC call: getAvailableCars()");

        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            CarListResponse response = stub.getAvailableCars(request);

            log.info("gRPC response: received {} cars", response.getCarsCount());
            return response.getCarsList();

        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {} - {}", e.getStatus().getCode(), e.getMessage());

            if (e.getStatus().getCode().equals(io.grpc.Status.Code.DEADLINE_EXCEEDED)) {
                throw new RuntimeException("StorageService timeout", e);
            }
            if (e.getStatus().getCode().equals(io.grpc.Status.Code.UNAVAILABLE)) {
                throw new RuntimeException("StorageService unavailable", e);
            }
            throw new RuntimeException("gRPC call failed: " + e.getMessage(), e);
        }
    }

    public CarResponse getCarById(String carId) {
        log.info("gRPC call: getCarById({})", carId);

        try {
            CarIdRequest request = CarIdRequest.newBuilder()
                    .setId(carId)
                    .build();

            CarResponse response = stub.getCarById(request);

            log.info("gRPC response: car {} found", carId);
            return response;

        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed for id {}: {} - {}", carId, e.getStatus().getCode(), e.getMessage());

            if (e.getStatus().getCode().equals(io.grpc.Status.Code.NOT_FOUND)) {
                return null;
            }
            if (e.getStatus().getCode().equals(io.grpc.Status.Code.DEADLINE_EXCEEDED)) {
                throw new RuntimeException("StorageService timeout", e);
            }
            if (e.getStatus().getCode().equals(io.grpc.Status.Code.UNAVAILABLE)) {
                throw new RuntimeException("StorageService unavailable", e);
            }
            throw new RuntimeException("gRPC call failed: " + e.getMessage(), e);
        }
    }
}