package application.dtos.response.carResponse.componentResponse;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarConfigurationResponse
{
    private String id;
    private String name;
    private String modelName;
    private String basePrice;
    private Double basePriceValue;
    private String totalPrice;
    private Double totalPriceValue;
    private List<ComponentResponse> baseComponents;
    private List<ComponentResponse> availableComponents;
}
