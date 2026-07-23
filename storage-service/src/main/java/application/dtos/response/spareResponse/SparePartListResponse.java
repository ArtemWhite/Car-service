package application.dtos.response.spareResponse;


import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparePartListResponse {
    private List<SparePartResponse> spareParts;
    private int totalCount;
    private int inStockCount;
    private int lowStockCount;
    private int outOfStockCount;
    private Map<String, Integer> countByType;
}