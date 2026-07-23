package presentation.controllers.carControllers;

import application.dtos.response.carResponse.CarResponse;
import application.services.carService.CarService;
import application.services.carService.client.CarClientService;
import presentation.dtos.request.carRequestPresentationDto.CarFilterPresentationRequest;
import presentation.dtos.response.carResponsePresentationDto.CarListConfigurationPresentationResponse;
import presentation.dtos.response.carResponsePresentationDto.CarListPresentationResponse;
import presentation.dtos.response.carResponsePresentationDto.CarPresentationResponse;
import presentation.mappers.CarPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
@Tag(name = "Cars (Public)", description = "Public car endpoints")
public class CarPublicController {

    private final CarService carService;
    private final CarClientService clientService;
    private final CarPresentationMapper mapper;

    @GetMapping("/{id}")
    @Operation(summary = "Get car by ID")
    public ResponseEntity<CarPresentationResponse> getCar(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toPresentation(carService.getCarById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all cars with filters")
    public ResponseEntity<CarListPresentationResponse> getAllCars(@Valid CarFilterPresentationRequest request) {
        var appFilter = mapper.toApplication(request);

        List<CarResponse> response = carService.getCarsWithFilters(appFilter);

        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available cars")
    public ResponseEntity<CarListPresentationResponse> getAvailableCars() {
        List<CarResponse> response = carService.getAvailableCars();
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/configurations/model/{modelId}")
    @Operation(summary = "Get configurations for a specific model")
    public ResponseEntity<CarListConfigurationPresentationResponse> getConfigurationsForModel(
            @PathVariable String modelId) {
        var response = clientService.getConfigurationsForModel(modelId);
        return ResponseEntity.ok(mapper.toConfigurationListPresentation(response));
    }
}