package application.dtos.response.carResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarListResponse
{
    private List<CarResponse> cars;
    private Integer totalCount;
    private Integer availableCount;
    private Integer testDriveCount;
    private Integer inStockCount;
}
