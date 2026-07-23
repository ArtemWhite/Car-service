package application.dtos.request.spareRequest;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparePartFilterRequest {
    private String spareType;
    private String manufacturer;
    private Double minPrice;
    private Double maxPrice;
    private String compatibleModelId;
    private Boolean inStock;
    private Boolean lowStock;
    //private Integer lowStockThreshold = 5;
    private String searchQuery;

    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}