package applicationTest.carServices.dto;

import application.dtos.request.carRequest.CreateCarRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateCarRequest DTO Tests")
class CreateCarRequestTest {

    @Test
    @DisplayName("Should create request with valid data")
    void shouldCreateRequestWithValidData() {
        CreateCarRequest request = new CreateCarRequest();

        request.setBrand("BMW");
        request.setModel("320i");
        request.setPrice(3500000.0);

        assertEquals("BMW", request.getBrand());
        assertEquals("320i", request.getModel());
        assertEquals(3500000.0, request.getPrice());
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        CreateCarRequest request = new CreateCarRequest();

        assertNull(request.getBrand());
        assertNull(request.getModel());
        assertNull(request.getPrice());
    }
}