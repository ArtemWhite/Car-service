package application.services.carService.manager;

import application.dtos.response.carResponse.CarResponse;
import application.dtos.response.carResponse.componentResponse.CarConfigurationResponse;

import java.util.List;

public interface CarManagerService
{
    void addCarToTestDriveFleet(String carId);
    void removeCarFromTestDriveFleet(String carId);
    List<CarResponse> getTestDriveFleet();
    List<CarResponse> getOrdersOnAvailableCars();
    List<CarConfigurationResponse> getOrdersOnConfigurationCars();
}