package application.dtos.response.spareResponse;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparePartCompatibilityResponse {
    private String sparePartId;
    private String sparePartName;
    private List<CompatibleModelDto> compatibleModels;
}

