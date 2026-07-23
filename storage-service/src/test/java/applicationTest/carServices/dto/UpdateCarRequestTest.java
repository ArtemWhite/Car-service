package applicationTest.carServices.dto;

import application.dtos.request.carRequest.UpdateCarRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("UpdateCarRequest DTO Tests")
class UpdateCarRequestTest {

    @Test
    @DisplayName("Should update only price")
    void shouldUpdateOnlyPrice() {
        UpdateCarRequest request = new UpdateCarRequest();
        request.setPrice(4000000.0);

        assertEquals(4000000.0, request.getPrice());
        assertNull(request.getStatus());
    }

    @Test
    @DisplayName("Should update only status")
    void shouldUpdateOnlyStatus() {
        UpdateCarRequest request = new UpdateCarRequest();
        request.setStatus("SOLD");

        assertEquals("SOLD", request.getStatus());
        assertNull(request.getPrice());
    }
}