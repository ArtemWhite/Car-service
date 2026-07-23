package infrastructure.grpc;

import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    @Bean
    public GrpcServerConfigurer grpcServerConfigurer() {
        return serverBuilder -> {
            serverBuilder.maxInboundMessageSize(10 * 1024 * 1024); // 10 MB
            serverBuilder.keepAliveTime(30, java.util.concurrent.TimeUnit.SECONDS);
            serverBuilder.keepAliveTimeout(5, java.util.concurrent.TimeUnit.SECONDS);
        };
    }
}