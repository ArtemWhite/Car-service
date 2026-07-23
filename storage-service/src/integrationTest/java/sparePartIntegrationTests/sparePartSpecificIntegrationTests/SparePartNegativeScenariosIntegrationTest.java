package sparePartIntegrationTests.sparePartSpecificIntegrationTests;

import org.springframework.http.MediaType;
import sparePartIntegrationTests.SparePartBaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SparePartNegativeScenariosIntegrationTest extends SparePartBaseIntegrationTest {

    @Test
    void shouldFailCreateWithoutName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-001");
        request.put("price", 1000.0);

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateWithoutSpareType() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "No Type Part");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-001");
        request.put("price", 1000.0);

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateWithoutManufacturer() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "No Manufacturer Part");
        request.put("partNumber", "TEST-001");
        request.put("price", 1000.0);

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateWithoutPartNumber() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "No Part Number Part");
        request.put("manufacturer", "Test");
        request.put("price", 1000.0);

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateWithoutPrice() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "No Price Part");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-001");

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateWithNegativePrice() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "Negative Price Part");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-001");
        request.put("price", -100.0);

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateWithNegativeQuantity() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "Negative Quantity Part");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-001");
        request.put("price", 1000.0);
        request.put("quantity", -5);

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateWithInvalidSpareType() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "INVALID_TYPE");
        request.put("name", "Invalid Type Part");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-001");
        request.put("price", 1000.0);

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailUpdateNonExistentSparePart() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Name");

        mockMvc.perform(put("/api/admin/spare-parts/{id}", UUID.randomUUID().toString())
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailDeleteNonExistentSparePart() throws Exception {
        mockMvc.perform(delete("/api/admin/spare-parts/{id}", UUID.randomUUID().toString())
                        .header("X-User-Id", adminId)
                        .param("reason", "Test"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailCreateWithoutAuth() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "No Auth Part");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-001");
        request.put("price", 1000.0);

        mockMvc.perform(post("/api/admin/spare-parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailGetSparePartByIdWhenNotFound() throws Exception {
        mockMvc.perform(get("/api/spare-parts/{id}", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }
}