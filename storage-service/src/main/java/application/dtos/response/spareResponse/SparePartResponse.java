package application.dtos.response.spareResponse;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SparePartResponse {
    private String id;
    private String spareType;
    private String spareTypeDisplayName;
    private String name;
    private String description;
    private String manufacturer;
    private String partNumber;
    private Double price;
    private String priceFormatted;
    private Integer quantity;
    private String status;
    private String statusDisplayName;
    private Integer compatibleModelsCount;
    private String sectionId;
    private String location;
    private LocalDateTime lastUpdated;

    private boolean inStock;
    private boolean lowStock;
    private boolean outOfStock;
}
