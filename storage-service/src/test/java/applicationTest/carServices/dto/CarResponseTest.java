package applicationTest.carServices.dto;

import application.dtos.response.carResponse.CarResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@DisplayName("CarResponse DTO Tests")
class CarResponseTest {

    @Test
    @DisplayName("Should create response via builder")
    void shouldCreateResponseViaBuilder() {
        CarResponse response = CarResponse.builder()
                .id("car123")
                .brand("BMW")
                .model("320i")
                .price(3500000.0)
                .priceFormatted("3 500 000 ₽")
                .availableForPurchase(true)
                .build();

        assertEquals("car123", response.getId());
        assertEquals("BMW", response.getBrand());
        assertEquals("320i", response.getModel());
        assertEquals(3500000.0, response.getPrice(), 0.001);
        assertEquals("3 500 000 ₽", response.getPriceFormatted());
        assertTrue(response.isAvailableForPurchase());
    }

    @Test
    @DisplayName("Should set all fields")
    void shouldSetAllFields() {
        CarResponse response = new CarResponse();

        response.setId("car123");
        response.setBrand("BMW");
        response.setModel("320i");
        response.setPrice(3500000.0);

        assertEquals("car123", response.getId());
        assertEquals("BMW", response.getBrand());
        assertEquals("320i", response.getModel());
        assertEquals(3500000.0, response.getPrice(), 0.001);
    }
}