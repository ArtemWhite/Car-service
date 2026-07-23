package application.services.carService.client;

import application.dtos.request.carRequest.ApplyConfigurationRequest;
import application.dtos.response.carResponse.CarResponse;
import application.dtos.response.carResponse.componentResponse.CarConfigurationResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface CarClientService {
    CarResponse applyConfiguration(ApplyConfigurationRequest request);
    List<CarConfigurationResponse> getConfigurationsForModel(String modelId);
    void sendTestDriveRequest(String carId, LocalDateTime requestedTime);
    void makeOrderOnCar(String carId);
}