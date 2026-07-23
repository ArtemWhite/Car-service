package application.dtos.request.carRequest;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarFilterRequest {
    private Double minPrice;
    private Double maxPrice;
    private String brand;
    private String model;
    private String bodyType;
    private String fuelType;
    private Integer minPower;
    private Integer maxPower;
    private Double minEngineVolume;
    private Double maxEngineVolume;
    private String transmissionType;
    private String driveType;
    private String color;
}
