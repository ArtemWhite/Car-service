package applicationTest.carServices.dto;

import application.dtos.request.carRequest.ApplyConfigurationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("ApplyConfigurationRequest DTO Tests")
class ApplyConfigurationRequestTest {

    @Test
    @DisplayName("Should create request with selected components")
    void shouldCreateRequestWithSelectedComponents() {
        ApplyConfigurationRequest request = new ApplyConfigurationRequest();
        request.setCarId("car123");
        request.setConfigurationId("config123");
        request.setSelectedComponents(Map.of("WHEELS", "wheel1"));

        assertEquals("car123", request.getCarId());
        assertEquals("config123", request.getConfigurationId());
        assertEquals(1, request.getSelectedComponents().size());
        assertEquals("wheel1", request.getSelectedComponents().get("WHEELS"));
    }

    @Test
    @DisplayName("Should create request without selected components")
    void shouldCreateRequestWithoutSelectedComponents() {
        ApplyConfigurationRequest request = new ApplyConfigurationRequest();
        request.setCarId("car123");
        request.setConfigurationId("config123");

        assertNull(request.getSelectedComponents());
    }
}