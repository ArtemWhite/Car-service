package applicationTest.carServices.dto;

import application.dtos.response.carResponse.CarListResponse;
import application.dtos.response.carResponse.CarResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

@DisplayName("CarListResponse DTO Tests")
class CarListResponseTest {

    @Test
    @DisplayName("Should create list response")
    void shouldCreateListResponse() {
        List<CarResponse> cars = List.of(
                CarResponse.builder().id("car1").build(),
                CarResponse.builder().id("car2").build()
        );

        CarListResponse response = new CarListResponse();
        response.setCars(cars);
        response.setTotalCount(2);

        assertEquals(2, response.getCars().size());
        assertEquals(2, response.getTotalCount().intValue());
    }
}