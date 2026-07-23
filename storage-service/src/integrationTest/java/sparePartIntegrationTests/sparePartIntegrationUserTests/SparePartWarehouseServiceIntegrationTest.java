package sparePartIntegrationTests.sparePartIntegrationUserTests;

import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import sparePartIntegrationTests.SparePartBaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SparePartWarehouseServiceIntegrationTest extends SparePartBaseIntegrationTest {

    @Test
    void shouldFindCompatibleSparePartsSuccessfully() throws Exception {
        String sparePartId = createSparePartDirect("Compatible Filter", "OIL_FILTER", 1000.0, 10, carModelId);

        UUID carModelUuid = UUID.fromString(carModelId);

        mockMvc.perform(get("/api/client/spare-parts/compatible/{carModelId}", carModelUuid.toString())
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].name").value("Compatible Filter"));
    }

    @Test
    void shouldThrowExceptionWhenReceiveNegativeQuantity() throws Exception {
        String sparePartId = createSparePart("Negative Receive Part", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/receive", sparePartId)
                        .header("X-User-Id", warehouseAdminId)
                        .param("quantity", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldWriteOff() throws Exception {
        String sparePartId = createSparePart("Write Off Part", "OIL_FILTER", 1000.0, 20);

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/write-off", sparePartId)
                        .header("X-User-Id", warehouseAdminId)
                        .param("quantity", "5")
                        .param("reason", "Damaged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    void shouldThrowExceptionWhenWriteOffMoreThanStock() throws Exception {
        String sparePartId = createSparePart("Low Stock Write Off", "OIL_FILTER", 1000.0, 5);

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/write-off", sparePartId)
                        .header("X-User-Id", warehouseAdminId)
                        .param("quantity", "10")
                        .param("reason", "Test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldMoveToLocation() throws Exception {
        String sparePartId = createSparePartDirect("Move Part", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(patch("/api/warehouse/spare-parts/{id}/location", sparePartId)
                        .header("X-User-Id", warehouseAdminId)
                        .param("section", "SEC-02")
                        .param("location", "B-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionId").value("SEC-02"))
                .andExpect(jsonPath("$.location").value("B-05"));
    }

    @Test
    void shouldUpdateStock() throws Exception {
        String sparePartId = createSparePart("Stock Update Part", "OIL_FILTER", 1000.0, 10);

        Map<String, Object> request = new HashMap<>();
        request.put("sparePartId", sparePartId);
        request.put("newQuantity", 50);
        request.put("reason", "Inventory adjustment");

        mockMvc.perform(patch("/api/warehouse/spare-parts/stock")
                        .header("X-User-Id", warehouseAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(50));
    }

    @Test
    void shouldThrowExceptionWhenNotWarehouseAdmin() throws Exception {
        String sparePartId = createSparePart("Unauthorized Stock Part", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/receive", sparePartId)
                        .header("X-User-Id", clientId)
                        .param("quantity", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should receive multiple shipments")
    void shouldReceiveMultipleShipments() throws Exception {
        String sparePartId = createSparePart("Multiple Shipment Part", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/receive", sparePartId)
                        .header("X-User-Id", warehouseAdminId)
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/receive", sparePartId)
                        .header("X-User-Id", warehouseAdminId)
                        .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(18));
    }

    @Test
    @DisplayName("Should write off to zero")
    void shouldWriteOffToZero() throws Exception {
        String sparePartId = createSparePart("Write Off To Zero", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/write-off", sparePartId)
                        .header("X-User-Id", warehouseAdminId)
                        .param("quantity", "10")
                        .param("reason", "Complete write off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(0))
                .andExpect(jsonPath("$.outOfStock").value(true));
    }

    @Test
    @DisplayName("Should update stock with section and location")
    void shouldUpdateStockWithLocation() throws Exception {
        String sparePartId = createSparePart("Update Stock Location", "OIL_FILTER", 1000.0, 10);

        Map<String, Object> request = new HashMap<>();
        request.put("sparePartId", sparePartId);
        request.put("newQuantity", 25);
        request.put("sectionId", "SEC-03");
        request.put("location", "C-12");
        request.put("reason", "Moved to new shelf");

        mockMvc.perform(patch("/api/warehouse/spare-parts/stock")
                        .header("X-User-Id", warehouseAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(25))
                .andExpect(jsonPath("$.sectionId").value("SEC-03"))
                .andExpect(jsonPath("$.location").value("C-12"));
    }
}