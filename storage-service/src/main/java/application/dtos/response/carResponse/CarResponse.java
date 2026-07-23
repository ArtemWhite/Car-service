package application.dtos.response.carResponse;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CarResponse
{
    private String id;

    private String brand;
    private String brandDisplayName;
    private String brandCountry;

    private String model;
    private String modelFullName;


    private String bodyType;
    private String bodyDisplayName;

    private String color;
    private String colorDisplayName;
    private Integer colorPrice;
    private boolean isDefaultColor;

    private String driveType;
    private String driveDisplayName;
    private String driveCode;

    private String engineFuelType;
    private String engineFuelDisplayName;
    private Double enginePower;
    private Double engineDisplacement;
    private String engineDescription;

    private String transmissionType;
    private String transmissionDisplayName;
    private Integer transmissionGears;
    private String transmissionDescription;

    private Double price;
    private String priceFormatted;
    private String currency;

    private String status;
    private String statusDisplayName;

    private String configurationId;
    private String configurationName;

    private boolean availableForPurchase;
    private boolean availableForTestDrive;
    private boolean inStock;
    private boolean reserved;
    private boolean sold;
    private String carInfo;
}
