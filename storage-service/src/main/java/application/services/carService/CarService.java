package application.services.carService;

import application.dtos.request.carRequest.CarFilterRequest;
import application.dtos.response.carResponse.CarResponse;

import java.util.List;

public interface CarService {
    CarResponse getCarById(String id);
    List<CarResponse> getAvailableCars();
    List<CarResponse> getCarsWithFilters(CarFilterRequest carFilterRequest);
}