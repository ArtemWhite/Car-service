package application.dtos.request.spareRequest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSparePartRequest {

    private String spareType;

    private String name;

    private String description;

    private String manufacturer;

    private String partNumber;

    private Double price;

    private String currency = "RUB";

    private Integer quantity = 0;

    private Set<String> compatibleModelIds;

    private String sectionId;
    private String location;
}