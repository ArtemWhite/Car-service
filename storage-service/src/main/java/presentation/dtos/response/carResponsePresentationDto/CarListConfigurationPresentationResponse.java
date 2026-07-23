package presentation.dtos.response.carResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@Schema(description = "Car configuration list response")
public class CarListConfigurationPresentationResponse {

    @Schema(description = "List of configurations")
    private List<CarConfigurationPresentationResponse> configurations;

    @Schema(description = "Total number of configurations", example = "5")
    private int totalCount;
}