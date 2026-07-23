package application.dtos.request.spareRequest;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {

    private String sparePartId;

    private Integer newQuantity;

    private String reason;

    private String sectionId;
    private String location;
}
