package application.dtos.response.spareResponse;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompatibleModelDto {
    private String modelId;
    private String modelName;
    private String brandName;
    private boolean compatible;
}