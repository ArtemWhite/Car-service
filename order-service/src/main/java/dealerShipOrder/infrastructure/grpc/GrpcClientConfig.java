package dealerShipOrder.infrastructure.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.storage-service.address:localhost:9090}")
    private String storageServiceAddress;

    @Bean
    public ManagedChannel managedChannel() {
        log.info("Creating gRPC channel to StorageService at: {}", storageServiceAddress);

        return ManagedChannelBuilder
                .forTarget(storageServiceAddress)
                .usePlaintext()  // Для разработки (без TLS)
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(5, TimeUnit.SECONDS)
                .maxInboundMessageSize(10 * 1024 * 1024)  // 10 MB
                .build();
    }
}