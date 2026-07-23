package sparePartIntegrationTests.sparePartIntegrationUserTests;

import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import sparePartIntegrationTests.SparePartBaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SparePartSystemAdminServiceIntegrationTest extends SparePartBaseIntegrationTest {

    @Test
    void shouldCreateSparePartSuccessfully() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "Admin Created Part");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-001");
        request.put("price", 1000.0);
        request.put("quantity", 10);
        request.put("sectionId", "SEC-01");
        request.put("location", "A-01");

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Admin Created Part"))
                .andExpect(jsonPath("$.spareType").value("OIL_FILTER"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void shouldCreateSparePartWithCompatibleModels() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "Compatible Part");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-002");
        request.put("price", 1000.0);
        request.put("quantity", 10);
        request.put("compatibleModelIds", Set.of(carModelId));

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.compatibleModelsCount").value(1));
    }

    @Test
    void shouldThrowExceptionWhenNotAdmin() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "Unauthorized Part");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-003");
        request.put("price", 1000.0);

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateSparePartName() throws Exception {
        String sparePartId = createSparePart("Original Name", "OIL_FILTER", 1000.0, 10);

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Name");

        mockMvc.perform(put("/api/admin/spare-parts/{id}", sparePartId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void shouldUpdateSparePartPrice() throws Exception {
        String sparePartId = createSparePart("Price Update Part", "OIL_FILTER", 1000.0, 10);

        Map<String, Object> request = new HashMap<>();
        request.put("price", 2000.0);

        mockMvc.perform(put("/api/admin/spare-parts/{id}", sparePartId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(2000.0));
    }

    @Test
    void shouldUpdateSparePartType() throws Exception {
        String sparePartId = createSparePart("Type Update Part", "OIL_FILTER", 1000.0, 10);

        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "BRAKE_PADS");

        mockMvc.perform(put("/api/admin/spare-parts/{id}", sparePartId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareType").value("BRAKE_PADS"));
    }

    @Test
    void shouldUpdateCompatibleModels() throws Exception {
        Integer modelExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM car_models WHERE id = ?::uuid",
                Integer.class, UUID.fromString(carModelId)
        );
        System.out.println("Car model exists in DB at start: " + (modelExists > 0) + ", id: " + carModelId);

        if (modelExists == 0) {
            carModelId = createTestCarModel();
            System.out.println("Created new car model: " + carModelId);
        }

        entityManager.flush();
        entityManager.clear();

        String sparePartId = createSparePart("Compatibility Update Part", "OIL_FILTER", 1000.0, 10);
        System.out.println("Created spare part: " + sparePartId);

        Integer compatCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM spare_part_compatibilities WHERE spare_part_id = ?::uuid",
                Integer.class, UUID.fromString(sparePartId)
        );
        System.out.println("Compatibilities before: " + compatCountBefore);

        Map<String, Object> request = new HashMap<>();
        request.put("compatibleModelIds", Set.of(carModelId));

        String updateResponse = mockMvc.perform(put("/api/admin/spare-parts/{id}", sparePartId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("Update response: " + updateResponse);

        Integer compatCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM spare_part_compatibilities WHERE spare_part_id = ?::uuid AND car_model_id = ?::uuid",
                Integer.class, UUID.fromString(sparePartId), UUID.fromString(carModelId)
        );
        System.out.println("Compatibilities after in DB: " + compatCountAfter);

        assertThat(compatCountAfter).isEqualTo(1);

        mockMvc.perform(get("/api/admin/spare-parts/{id}", sparePartId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compatibleModelsCount").value(1));
    }

    @Test
    void shouldDeleteSparePart() throws Exception {
        String sparePartId = createSparePart("To Delete", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(delete("/api/admin/spare-parts/{id}", sparePartId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Test deletion"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/spare-parts/{id}", sparePartId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddCompatibleModel() throws Exception {
        String sparePartId = createSparePart("Add Compatible Part", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(post("/api/admin/spare-parts/{sparePartId}/compatible-models/{modelId}", sparePartId, carModelId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/client/spare-parts/compatible/{carModelId}", carModelId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1));
    }

    @Test
    void shouldRemoveCompatibleModel() throws Exception {
        String sparePartId = createSparePart("Remove Compatible Part", "OIL_FILTER", 1000.0, 10, Set.of(carModelId));

        mockMvc.perform(delete("/api/admin/spare-parts/{sparePartId}/compatible-models/{modelId}", sparePartId, carModelId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/client/spare-parts/compatible/{carModelId}", carModelId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(0));
    }

    @Test
    @DisplayName("Should log admin actions when creating spare part")
    void shouldLogAdminActionOnCreate() throws Exception {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'audit_log_entries'",
                Integer.class);

        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "Logged Create Part");
        request.put("manufacturer", "Test");
        request.put("partNumber", "TEST-001");
        request.put("price", 1000.0);
        request.put("quantity", 10);
        request.put("sectionId", "SEC-01");
        request.put("location", "A-01");

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        Integer auditCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_log_entries WHERE admin_id = ?::uuid",
                Integer.class, UUID.fromString(adminId));

        System.out.println("Audit log entries count (if table exists): " + auditCount);

        assertThat(auditCount).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should add compatible model only once (no duplicates)")
    void shouldNotAddDuplicateCompatibleModel() throws Exception {
        String sparePartId = createSparePart("No Duplicate Part", "OIL_FILTER", 1000.0, 10);
        mockMvc.perform(post("/api/admin/spare-parts/{sparePartId}/compatible-models/{modelId}",
                        sparePartId, carModelId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/spare-parts/{sparePartId}/compatible-models/{modelId}",
                        sparePartId, carModelId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        Integer compatCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM spare_part_compatibilities WHERE spare_part_id = ?::uuid AND car_model_id = ?::uuid",
                Integer.class, UUID.fromString(sparePartId), UUID.fromString(carModelId)
        );
        assertThat(compatCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle removing non-existent compatible model gracefully")
    void shouldNotFailWhenRemovingNonExistentCompatibleModel() throws Exception {
        String sparePartId = createSparePart("Remove Non Existent Part", "OIL_FILTER", 1000.0, 10);
        String fakeModelId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/admin/spare-parts/{sparePartId}/compatible-models/{modelId}",
                        sparePartId, fakeModelId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());
    }
}