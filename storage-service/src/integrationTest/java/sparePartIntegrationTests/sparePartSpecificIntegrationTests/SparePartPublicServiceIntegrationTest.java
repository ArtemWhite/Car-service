package sparePartIntegrationTests.sparePartSpecificIntegrationTests;

import org.junit.jupiter.api.DisplayName;
import sparePartIntegrationTests.SparePartBaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SparePartPublicServiceIntegrationTest extends SparePartBaseIntegrationTest {

    @Test
    void shouldGetSparePartById() throws Exception {
        String sparePartId = createSparePart("Public Part", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(get("/api/spare-parts/{id}", sparePartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sparePartId))
                .andExpect(jsonPath("$.name").value("Public Part"));
    }

    @Test
    void shouldGetAllSpareParts() throws Exception {
        createSparePart("Part 1", "OIL_FILTER", 1000.0, 10);
        createSparePart("Part 2", "BRAKE_PADS", 2000.0, 5);

        mockMvc.perform(get("/api/spare-parts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(2));
    }

    @Test
    void shouldFilterByType() throws Exception {
        createSparePart("Oil Filter 1", "OIL_FILTER", 1000.0, 10);
        createSparePart("Oil Filter 2", "OIL_FILTER", 1500.0, 5);
        createSparePart("Brake Pads", "BRAKE_PADS", 2000.0, 5);

        mockMvc.perform(get("/api/spare-parts")
                        .param("spareType", "OIL_FILTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(2));
    }

    @Test
    void shouldFilterByMinPrice() throws Exception {
        createSparePart("Cheap Part", "OIL_FILTER", 500.0, 10);
        createSparePart("Medium Part", "OIL_FILTER", 1000.0, 10);
        createSparePart("Expensive Part", "OIL_FILTER", 2000.0, 10);

        mockMvc.perform(get("/api/spare-parts")
                        .param("minPrice", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(2));
    }

    @Test
    void shouldFilterByMaxPrice() throws Exception {
        createSparePart("Cheap Part", "OIL_FILTER", 500.0, 10);
        createSparePart("Medium Part", "OIL_FILTER", 1000.0, 10);
        createSparePart("Expensive Part", "OIL_FILTER", 2000.0, 10);

        mockMvc.perform(get("/api/spare-parts")
                        .param("maxPrice", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(2));
    }

    @Test
    void shouldFilterByPriceRange() throws Exception {
        createSparePart("Cheap Part", "OIL_FILTER", 500.0, 10);
        createSparePart("Medium Part", "OIL_FILTER", 1000.0, 10);
        createSparePart("Expensive Part", "OIL_FILTER", 2000.0, 10);

        mockMvc.perform(get("/api/spare-parts")
                        .param("minPrice", "800")
                        .param("maxPrice", "1500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].name").value("Medium Part"));
    }

    @Test
    void shouldFilterByManufacturer() throws Exception {
        String partId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO spare_parts (id, type_id, name, description, manufacturer, part_number, price, currency, stock_quantity, section_id, location, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, (SELECT id FROM spare_types WHERE name = 'OIL_FILTER'), 'Bosch Filter', 'Desc', 'Bosch', 'BOSCH-001', 1000.00, 'RUB', 10, 'SEC-01', 'A-01', NOW(), NOW(), false)",
                UUID.fromString(partId)
        );

        createSparePart("Other Filter", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(get("/api/spare-parts")
                        .param("manufacturer", "Bosch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1));
    }

    @Test
    void shouldFilterByInStock() throws Exception {
        createSparePart("In Stock Part", "OIL_FILTER", 1000.0, 10);
        createSparePart("Out Stock Part", "OIL_FILTER", 1000.0, 0);

        mockMvc.perform(get("/api/spare-parts")
                        .param("inStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].name").value("In Stock Part"));
    }

    @Test
    void shouldFilterByLowStock() throws Exception {
        createSparePart("Low Stock Part", "OIL_FILTER", 1000.0, 3);
        createSparePart("Normal Stock Part", "OIL_FILTER", 1000.0, 10);
        createSparePart("Out Stock Part", "OIL_FILTER", 1000.0, 0);

        mockMvc.perform(get("/api/spare-parts")
                        .param("lowStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].name").value("Low Stock Part"));
    }

    @Test
    void shouldFilterByCompatibleModel() throws Exception {
        createSparePart("Compatible Part", "OIL_FILTER", 1000.0, 10, Set.of(carModelId));
        createSparePart("Incompatible Part", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(get("/api/spare-parts")
                        .param("compatibleModelId", carModelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].name").value("Compatible Part"));
    }

    @Test
    void shouldFilterBySearchQuery() throws Exception {
        createSparePart("Brake Pads Premium", "BRAKE_PADS", 2000.0, 10);
        createSparePart("Oil Filter", "OIL_FILTER", 1000.0, 10);

        mockMvc.perform(get("/api/spare-parts")
                        .param("searchQuery", "Brake"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].name").value("Brake Pads Premium"));
    }

    @Test
    void shouldCombineMultipleFilters() throws Exception {
        createSparePart("Target Part", "BRAKE_PADS", 1500.0, 5, Set.of(carModelId));
        createSparePart("Other Part 1", "OIL_FILTER", 1000.0, 10);
        createSparePart("Other Part 2", "BRAKE_PADS", 3000.0, 5, Set.of(carModelId));

        mockMvc.perform(get("/api/spare-parts")
                        .param("spareType", "BRAKE_PADS")
                        .param("minPrice", "1000")
                        .param("maxPrice", "2000")
                        .param("compatibleModelId", carModelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].name").value("Target Part"));
    }

    @Test
    void shouldPaginateResults() throws Exception {
        for (int i = 1; i <= 25; i++) {
            createSparePart("Pagination Part " + i, "OIL_FILTER", 1000.0, 10);
        }

        mockMvc.perform(get("/api/spare-parts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(10));
    }

    @Test
    void shouldSortResults() throws Exception {
        createSparePart("A Part", "OIL_FILTER", 1000.0, 10);
        createSparePart("B Part", "OIL_FILTER", 2000.0, 10);
        createSparePart("C Part", "OIL_FILTER", 500.0, 10);

        mockMvc.perform(get("/api/spare-parts")
                        .param("sortBy", "price")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts[0].price").value(500.0))
                .andExpect(jsonPath("$.spareParts[2].price").value(2000.0));
    }

    @Test
    void shouldGetSparePartsByType() throws Exception {
        createSparePart("Oil Filter 1", "OIL_FILTER", 1000.0, 10);
        createSparePart("Oil Filter 2", "OIL_FILTER", 1500.0, 5);
        createSparePart("Brake Pads", "BRAKE_PADS", 2000.0, 5);

        mockMvc.perform(get("/api/spare-parts/type/OIL_FILTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(2));
    }

    @Test
    void shouldReturnEmptyListWhenInvalidType() throws Exception {
        mockMvc.perform(get("/api/spare-parts/type/INVALID_TYPE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(0));
    }

    @Test
    @DisplayName("Should be compatible with multiple car models")
    void shouldBeCompatibleWithMultipleModels() throws Exception {
        String modelId1 = createTestCarModel();
        String modelId2 = createTestCarModel();

        String sparePartId = createSparePart("Multi Compat Part", "OIL_FILTER",
                1000.0, 10, Set.of(modelId1, modelId2));

        mockMvc.perform(get("/api/spare-parts")
                        .param("compatibleModelId", modelId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].id").value(sparePartId));

        mockMvc.perform(get("/api/spare-parts")
                        .param("compatibleModelId", modelId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1))
                .andExpect(jsonPath("$.spareParts[0].id").value(sparePartId));
    }

    @Test
    @DisplayName("Should handle special characters in search query")
    void shouldHandleSpecialCharactersInSearch() throws Exception {
        String partId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO spare_parts (id, type_id, name, description, manufacturer, part_number, price, currency, stock_quantity, section_id, location, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, (SELECT id FROM spare_types WHERE name = 'OIL_FILTER'), 'Part with $pecial Char!', 'Desc', 'Test', 'TEST-001', 1000.00, 'RUB', 10, 'SEC-01', 'A-01', NOW(), NOW(), false)",
                UUID.fromString(partId)
        );

        mockMvc.perform(get("/api/spare-parts")
                        .param("searchQuery", "$pecial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(1));
    }

    @Test
    @DisplayName("Should return empty list when sortBy field is invalid (use default sorting)")
    void shouldUseDefaultSortingWhenInvalidFieldProvided() throws Exception {
        createSparePart("A Part", "OIL_FILTER", 500.0, 10);
        createSparePart("B Part", "OIL_FILTER", 1000.0, 10);
        createSparePart("C Part", "OIL_FILTER", 1500.0, 10);

        mockMvc.perform(get("/api/spare-parts")
                        .param("sortBy", "invalid_field")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(3));
    }

    @Test
    @DisplayName("Should paginate correctly on last page")
    void shouldPaginateCorrectlyOnLastPage() throws Exception {
        for (int i = 1; i <= 25; i++) {
            createSparePart("Last Page Part " + i, "OIL_FILTER", 1000.0, 10);
        }

        mockMvc.perform(get("/api/spare-parts")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(5));
    }

    @Test
    @DisplayName("Should return empty list when page is out of range")
    void shouldReturnEmptyListWhenPageOutOfRange() throws Exception {
        for (int i = 1; i <= 5; i++) {
            createSparePart("Page Out Part " + i, "OIL_FILTER", 1000.0, 10);
        }

        mockMvc.perform(get("/api/spare-parts")
                        .param("page", "10")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spareParts.length()").value(0));
    }
}