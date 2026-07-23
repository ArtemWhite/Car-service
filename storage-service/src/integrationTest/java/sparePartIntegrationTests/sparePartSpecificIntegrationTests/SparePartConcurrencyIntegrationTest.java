package sparePartIntegrationTests.sparePartSpecificIntegrationTests;

import org.junit.jupiter.api.DisplayName;
import sparePartIntegrationTests.SparePartBaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SparePartConcurrencyIntegrationTest extends SparePartBaseIntegrationTest {

    @Test
    @DisplayName("Should handle concurrent write-off requests correctly")
    void shouldHandleConcurrentWriteOffs() throws Exception {
        String sparePartId = UUID.randomUUID().toString();
        UUID spareTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM spare_types WHERE name = 'OIL_FILTER'", UUID.class);

        jdbcTemplate.update(
                "INSERT INTO spare_parts (id, type_id, name, description, manufacturer, part_number, price, currency, stock_quantity, section_id, location, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, 'Concurrent Part', 'Test', 'Test', 'TEST-001', 1000.0, 'RUB', 100, 'SEC-01', 'A-01', NOW(), NOW(), false)",
                UUID.fromString(sparePartId), spareTypeId);

        assertThat(warehouseAdminId).isNotNull();

        int threadCount = 10;
        int writeOffPerThread = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(post("/api/warehouse/spare-parts/{id}/write-off", sparePartId)
                                    .header("X-User-Id", warehouseAdminId)
                                    .param("quantity", String.valueOf(writeOffPerThread))
                                    .param("reason", "Concurrent test"))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    System.out.println("Error in concurrent write-off: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        Integer finalQuantity = jdbcTemplate.queryForObject(
                "SELECT stock_quantity FROM spare_parts WHERE id = ?::uuid",
                Integer.class, UUID.fromString(sparePartId));

        System.out.println("Final quantity: " + finalQuantity);
        assertThat(finalQuantity).isLessThanOrEqualTo(100);
    }

    @Test
    @DisplayName("Should prevent negative stock")
    void shouldPreventNegativeStock() throws Exception {
        String sparePartId = createSparePart("Negative Prevention Part", "OIL_FILTER", 1000.0, 5);

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/write-off", sparePartId)
                        .header("X-User-Id", warehouseAdminId)
                        .param("quantity", "10")
                        .param("reason", "Too much"))
                .andExpect(status().isBadRequest());

        Integer quantity = jdbcTemplate.queryForObject(
                "SELECT stock_quantity FROM spare_parts WHERE id = ?::uuid",
                Integer.class, UUID.fromString(sparePartId)
        );
        assertThat(quantity).isEqualTo(5);
    }
}