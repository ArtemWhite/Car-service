package sparePartIntegrationTests.sparePartIntegrationUserTests;

import domain.models.car.CarModel;
import domain.repository.carRepository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import sparePartIntegrationTests.SparePartBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SparePartClientServiceIntegrationTest extends SparePartBaseIntegrationTest {

    @Autowired
    private CarRepository carRepository;

    private String testCarModelId;
    private String testSparePartId;

    @BeforeEach
    void setUp() throws Exception {

        testCarModelId = createTestCarModelWithBrand();

        testSparePartId = createSparePartViaApiWithCompatibility();
    }

    private String createTestCarModelWithBrand() {
        String brandName = "BMW";
        UUID brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_brands WHERE name = ?",
                UUID.class, brandName);

        if (brandId == null) {
            brandId = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, ?, ?, 'Germany', NOW(), NOW(), false)",
                    brandId, brandName, brandName);
        }

        String uniqueModelName = "TEST_MODEL_" + UUID.randomUUID().toString().substring(0, 8);
        UUID modelId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, ?::uuid, 'G05', NOW(), NOW(), false)",
                modelId, uniqueModelName, brandId);

        entityManager.flush();
        entityManager.clear();

        CarModel model = carRepository.findModelById(modelId.toString())
                .orElseThrow(() -> new RuntimeException("Model not found after creation!"));
        System.out.println("Model created and found: " + model.getId() + " - " + model.getName());

        return modelId.toString();
    }

    private String createSparePartViaApiWithCompatibility() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "OIL_FILTER");
        request.put("name", "Compatible Filter");
        request.put("description", "Test compatible oil filter");
        request.put("manufacturer", "Test Manufacturer");
        request.put("partNumber", "PN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.put("price", 1000.0);
        request.put("quantity", 10);
        request.put("sectionId", "SEC-01");
        request.put("location", "A-01");
        request.put("compatibleModelIds", Set.of(testCarModelId));

        String response = mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sparePartId = objectMapper.readTree(response).get("id").asText();
        System.out.println("Created spare part via API: " + sparePartId);

        Integer compatCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM spare_part_compatibilities WHERE spare_part_id = ?::uuid AND car_model_id = ?::uuid",
                Integer.class, UUID.fromString(sparePartId), UUID.fromString(testCarModelId));
        System.out.println("Compatibility count: " + compatCount);

        entityManager.flush();
        entityManager.clear();

        return sparePartId;
    }

    @Test
    void shouldFindCompatibleSparePartsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/client/spare-parts/compatible/{carModelId}", testCarModelId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].id").value(testSparePartId))
                .andExpect(jsonPath("$.spareParts[0].name").value("Compatible Filter"));
    }

    @Test
    void shouldReturnEmptyListWhenNoCompatibleSpareParts() throws Exception {
        String newCarModelId = createTestCarModelWithBrand();

        mockMvc.perform(get("/api/client/spare-parts/compatible/{carModelId}", newCarModelId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(0));
    }

    @Test
    void shouldThrowExceptionWhenCarModelNotFound() throws Exception {
        mockMvc.perform(get("/api/client/spare-parts/compatible/{carModelId}", UUID.randomUUID().toString())
                        .header("X-User-Id", clientId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetSparePartDetailsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/client/spare-parts/{id}/details", testSparePartId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSparePartId))
                .andExpect(jsonPath("$.name").value("Compatible Filter"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void shouldThrowExceptionWhenSparePartNotFoundForDetails() throws Exception {
        mockMvc.perform(get("/api/client/spare-parts/{id}/details", UUID.randomUUID().toString())
                        .header("X-User-Id", clientId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchSparePartsByName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "BRAKE_PADS");
        request.put("name", "Searchable Brake Pads");
        request.put("description", "Test brake pads");
        request.put("manufacturer", "Test Manufacturer");
        request.put("partNumber", "PN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.put("price", 2000.0);
        request.put("quantity", 5);
        request.put("sectionId", "SEC-01");
        request.put("location", "A-01");

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/client/spare-parts/search")
                        .header("X-User-Id", clientId)
                        .param("query", "Brake"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].name").value("Searchable Brake Pads"));
    }

    @Test
    void shouldReturnEmptyListWhenSearchNoResults() throws Exception {
        mockMvc.perform(get("/api/client/spare-parts/search")
                        .header("X-User-Id", clientId)
                        .param("query", "NonExistentPartXYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(0));
    }

    @Test
    void shouldSearchWithEmptyQuery() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", "BRAKE_PADS");
        request.put("name", "Part A");
        request.put("description", "Test part A");
        request.put("manufacturer", "Test Manufacturer");
        request.put("partNumber", "PN-A" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.put("price", 2000.0);
        request.put("quantity", 5);
        request.put("sectionId", "SEC-01");
        request.put("location", "A-01");

        mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/client/spare-parts/search")
                        .header("X-User-Id", clientId)
                        .param("query", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(2)); // Compatible Filter + Part A
    }

    @Test
    void shouldIncludeStockQuantityInResponse() throws Exception {
        mockMvc.perform(get("/api/client/spare-parts/{id}/details", testSparePartId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.inStock").value(true));
    }

    @Test
    void shouldIncludeLocationInResponse() throws Exception {
        mockMvc.perform(get("/api/client/spare-parts/{id}/details", testSparePartId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionId").value("SEC-01"))
                .andExpect(jsonPath("$.location").value("A-01"));
    }
}