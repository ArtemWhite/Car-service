package presentation.dtos.request.carRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to filter cars with pagination")
public class CarFilterPresentationRequest {

    @Schema(description = "Minimum price", example = "1000000.0")
    private Double minPrice;

    @Schema(description = "Maximum price", example = "5000000.0")
    private Double maxPrice;

    @Schema(description = "Car brand", example = "Toyota")
    private String brand;

    @Schema(description = "Car model", example = "Camry")
    private String model;

    @Schema(description = "Body type", example = "SUV")
    private String bodyType;

    @Schema(description = "Fuel type", example = "HYBRID")
    private String fuelType;

    @Schema(description = "Minimum engine power (HP)", example = "150")
    private Integer minPower;

    @Schema(description = "Maximum engine power (HP)", example = "300")
    private Integer maxPower;

    @Schema(description = "Minimum engine displacement (L)", example = "1.6")
    private Double minEngineVolume;

    @Schema(description = "Maximum engine displacement (L)", example = "3.0")
    private Double maxEngineVolume;

    @Schema(description = "Transmission type", example = "AUTOMATIC")
    private String transmissionType;

    @Schema(description = "Drive type", example = "AWD")
    private String driveType;

    @Schema(description = "Color", example = "BLACK")
    private String color;

    @Schema(description = "Car status", example = "AVAILABLE")
    private String status;

    @Min(value = 0, message = "Page number must be 0 or greater")
    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private Integer size = 20;

    @Schema(description = "Sort field", example = "price", defaultValue = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "Sort direction", example = "DESC", defaultValue = "DESC")
    private String sortDirection = "DESC";
}