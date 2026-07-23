package application.dtos.request.carRequest;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCarRequest
{
    private Double price;
    private String status;
    private String configurationId;
    private String updateReason;
}
