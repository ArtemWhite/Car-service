package application.dtos.request.spareRequest;

import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSparePartRequest {

    private String name;
    private String description;
    private String manufacturer;
    private String partNumber;

    private Double price;

    private String spareType;
    private Set<String> compatibleModelIds;
    private String updateReason;
}