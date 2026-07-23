package application.dtos.request.carRequest;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyConfigurationRequest
{
    private String carId;
    private String configurationId;
    private String clientId;
    private Map<String, String> selectedComponents;
}
