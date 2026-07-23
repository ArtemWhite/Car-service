package sparePartIntegrationTests.sparePartIntegrationUserTests;

import org.junit.jupiter.api.DisplayName;
import sparePartIntegrationTests.SparePartBaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SparePartManagerServiceIntegrationTest extends SparePartBaseIntegrationTest {

    @Test
    void shouldGetLowStockParts() throws Exception {
        createSparePart("Low Stock Part 1", "OIL_FILTER", 1000.0, 3);
        createSparePart("Low Stock Part 2", "BRAKE_PADS", 2000.0, 2);
        createSparePart("Normal Stock Part", "OIL_FILTER", 1000.0, 50);

        mockMvc.perform(get("/api/manager/spare-parts/low-stock")
                        .header("X-User-Id", managerId)
                        .param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(2));
    }

    @Test
    void shouldGetLowStockPartsWithCustomThreshold() throws Exception {
        createSparePart("Low Stock Part", "OIL_FILTER", 1000.0, 8);
        createSparePart("Normal Part", "OIL_FILTER", 1000.0, 20);

        mockMvc.perform(get("/api/manager/spare-parts/low-stock")
                        .header("X-User-Id", managerId)
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1));
    }

    @Test
    void shouldReturnEmptyListWhenNoLowStock() throws Exception {
        createSparePart("High Stock Part", "OIL_FILTER", 1000.0, 100);

        mockMvc.perform(get("/api/manager/spare-parts/low-stock")
                        .header("X-User-Id", managerId)
                        .param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(0));
    }

    @Test
    void shouldGetOutOfStockParts() throws Exception {
        createSparePart("Out Stock Part 1", "OIL_FILTER", 1000.0, 0);
        createSparePart("Out Stock Part 2", "BRAKE_PADS", 2000.0, 0);
        createSparePart("In Stock Part", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(get("/api/manager/spare-parts/out-of-stock")
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(2));
    }

    @Test
    void shouldRequestRestockSuccessfully() throws Exception {
        String sparePartId = createSparePart("Restock Part", "OIL_FILTER", 1000.0, 2);

        mockMvc.perform(post("/api/manager/spare-parts/{id}/restock", sparePartId)
                        .header("X-User-Id", managerId)
                        .param("quantity", "50"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldThrowExceptionWhenNotManager() throws Exception {
        String sparePartId = createSparePart("Restock Part", "OIL_FILTER", 1000.0, 2);

        mockMvc.perform(post("/api/manager/spare-parts/{id}/restock", sparePartId)
                        .header("X-User-Id", clientId)
                        .param("quantity", "50"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get low stock with default threshold")
    void shouldGetLowStockWithDefaultThreshold() throws Exception {
        createSparePart("Default Low Stock", "OIL_FILTER", 1000.0, 3);
        createSparePart("Default Normal Stock", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(get("/api/manager/spare-parts/low-stock")
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1));
    }

    @Test
    @DisplayName("Should request restock with zero quantity (should fail)")
    void shouldFailRestockWithZeroQuantity() throws Exception {
        String sparePartId = createSparePart("Zero Restock Part", "OIL_FILTER", 1000.0, 2);

        mockMvc.perform(post("/api/manager/spare-parts/{id}/restock", sparePartId)
                        .header("X-User-Id", managerId)
                        .param("quantity", "0"))
                .andExpect(status().isBadRequest());
    }
}