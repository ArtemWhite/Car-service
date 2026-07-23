package applicationTest.carServices.dto;

import application.dtos.request.carRequest.CarFilterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("CarFilterRequest DTO Tests")
class CarFilterRequestTest {

    @Test
    @DisplayName("Should create filter with all fields")
    void shouldCreateFilterWithAllFields() {
        CarFilterRequest request = new CarFilterRequest();
        request.setBrand("BMW");
        request.setModel("320i");
        request.setMinPrice(3000000.0);
        request.setMaxPrice(4000000.0);

        assertEquals("BMW", request.getBrand());
        assertEquals("320i", request.getModel());
        assertEquals(3000000.0, request.getMinPrice());
        assertEquals(4000000.0, request.getMaxPrice());
    }

    @Test
    @DisplayName("Should create empty filter")
    void shouldCreateEmptyFilter() {
        CarFilterRequest request = new CarFilterRequest();

        assertNull(request.getBrand());
        assertNull(request.getModel());
        assertNull(request.getMinPrice());
        assertNull(request.getMaxPrice());
    }
}