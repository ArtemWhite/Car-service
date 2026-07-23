package application.dtos.request.carRequest;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCarRequest
{
    private String brand;

    private String model;

    private String bodyType;

    private String color;

    private String driveType;

    private String engineFuelType;

    private Double enginePower;

    private Double engineDisplacement;

    private Integer transmissionGears;

    private String transmissionType;

    private Double price;

    private String configurationId;
}
