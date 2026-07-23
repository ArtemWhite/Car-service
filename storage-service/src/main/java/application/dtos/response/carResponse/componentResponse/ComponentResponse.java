package application.dtos.response.carResponse.componentResponse;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentResponse
{
    private String id;
    private String type;
    private String typeDisplayName;
    private String name;
    private String description;
    private String price;
    private Double priceValue;
    private boolean selected;
    private boolean compatible;
}
