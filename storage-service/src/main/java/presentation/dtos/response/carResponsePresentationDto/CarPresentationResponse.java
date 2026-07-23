package presentation.dtos.response.carResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Car response")
public class CarPresentationResponse {

    @Schema(description = "Car ID", example = "car_123e4567")
    private String id;

    @Schema(description = "Car brand", example = "Toyota")
    private String brand;

    @Schema(description = "Brand display name", example = "Toyota Motor Corporation")
    private String brandDisplayName;

    @Schema(description = "Brand country", example = "Japan")
    private String brandCountry;

    @Schema(description = "Car model", example = "Camry")
    private String model;

    @Schema(description = "Full model name", example = "Toyota Camry XV70")
    private String modelFullName;

    @Schema(description = "Body type code", example = "SEDAN")
    private String bodyType;

    @Schema(description = "Body type display name", example = "Sedan")
    private String bodyDisplayName;

    @Schema(description = "Color code", example = "BLACK")
    private String color;

    @Schema(description = "Color display name", example = "Black Pearl")
    private String colorDisplayName;

    @Schema(description = "Color price premium", example = "30000")
    private Integer colorPrice;

    @Schema(description = "Whether this is default color", example = "false")
    private boolean isDefaultColor;

    @Schema(description = "Drive type code", example = "FWD")
    private String driveType;

    @Schema(description = "Drive type display name", example = "Front-Wheel Drive")
    private String driveDisplayName;

    @Schema(description = "Drive code", example = "FWD")
    private String driveCode;

    @Schema(description = "Engine fuel type code", example = "HYBRID")
    private String engineFuelType;

    @Schema(description = "Engine fuel type display name", example = "Hybrid")
    private String engineFuelDisplayName;

    @Schema(description = "Engine power (HP)", example = "218.0")
    private Double enginePower;

    @Schema(description = "Engine displacement (L)", example = "2.5")
    private Double engineDisplacement;

    @Schema(description = "Engine description", example = "2.5L Hybrid Dynamic Force Engine")
    private String engineDescription;

    @Schema(description = "Transmission type code", example = "CVT")
    private String transmissionType;

    @Schema(description = "Transmission type display name", example = "Continuously Variable Transmission")
    private String transmissionDisplayName;

    @Schema(description = "Number of gears", example = "8")
    private Integer transmissionGears;

    @Schema(description = "Transmission description", example = "8-speed Direct Shift")
    private String transmissionDescription;

    @Schema(description = "Car price", example = "2500000.0")
    private Double price;

    @Schema(description = "Formatted price", example = "2,500,000 ₽")
    private String priceFormatted;

    @Schema(description = "Currency", example = "RUB")
    private String currency;

    @Schema(description = "Status code", example = "AVAILABLE")
    private String status;

    @Schema(description = "Status display name", example = "Available")
    private String statusDisplayName;

    @Schema(description = "Configuration ID", example = "cfg_123e4567")
    private String configurationId;

    @Schema(description = "Configuration name", example = "Luxury Package")
    private String configurationName;

    @Schema(description = "Available for purchase", example = "true")
    private boolean availableForPurchase;

    @Schema(description = "Available for test drive", example = "true")
    private boolean availableForTestDrive;

    @Schema(description = "In stock", example = "true")
    private boolean inStock;

    @Schema(description = "Reserved", example = "false")
    private boolean reserved;

    @Schema(description = "Sold", example = "false")
    private boolean sold;

    @Schema(description = "Car info summary", example = "Toyota Camry 2024, 2.5L Hybrid, 218 hp")
    private String carInfo;

    @Schema(description = "Created date")
    private LocalDateTime createdAt;

    @Schema(description = "Updated date")
    private LocalDateTime updatedAt;
}