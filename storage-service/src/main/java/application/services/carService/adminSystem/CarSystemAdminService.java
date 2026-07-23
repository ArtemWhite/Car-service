package application.services.carService.adminSystem;

import application.dtos.request.carRequest.CreateCarRequest;
import application.dtos.request.carRequest.UpdateCarRequest;
import application.dtos.response.carResponse.CarResponse;

import java.util.List;

public interface CarSystemAdminService
{
    CarResponse createCar(CreateCarRequest createCarRequest);
    CarResponse updateCar(String carId, UpdateCarRequest updateCarRequest);
    void deleteCar(String carId, String reason);
    CarResponse changeCarStatus(String carId, String status);
    List<CarResponse> getAllCars();
}