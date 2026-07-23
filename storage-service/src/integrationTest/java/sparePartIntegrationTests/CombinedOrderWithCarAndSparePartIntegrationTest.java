package sparePartIntegrationTests;

import org.junit.jupiter.api.Disabled;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class CombinedOrderWithCarAndSparePartIntegrationTest extends SparePartBaseIntegrationTest {

    private String customerId;
    private String testManagerId;
    private String testAdminId;
    private String testWarehouseAdminId;

    @BeforeEach
    void setUp() throws Exception {
        super.baseSetUp();
        customerId = clientId;
        testManagerId = managerId;
        testAdminId = adminId;
        testWarehouseAdminId = warehouseAdminId;

        System.out.println("customerId: " + customerId);
        System.out.println("testManagerId: " + testManagerId);
        System.out.println("testAdminId: " + testAdminId);
        System.out.println("testWarehouseAdminId: " + testWarehouseAdminId);
    }

    @Disabled("Requires order-service endpoints (/api/client/orders, /api/admin/orders, etc.) to be available")
    @Test
    void shouldCreateCompleteOrderWithCarAndSparePart() throws Exception {
        String carRequest = """
            {
                "brand": "BMW",
                "model": "X5",
                "bodyType": "SEDAN",
                "color": "BLACK",
                "driveType": "FRONT",
                "engineFuelType": "PETROL",
                "enginePower": 249.0,
                "engineDisplacement": 2.0,
                "transmissionGears": 8,
                "transmissionType": "AUTOMATIC",
                "price": 3500000.00
            }
            """;

        String carResponse = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", testAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String carId = objectMapper.readTree(carResponse).get("id").asText();

        mockMvc.perform(put("/api/admin/cars/{id}", carId)
                        .header("X-User-Id", testAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"AVAILABLE\"}"))
                .andExpect(status().isOk());

        String sparePartId = createSparePart("Winter Tires Set", "TIRE", 25000.0, 4, Set.of(carModelId));

        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("carId", carId);
        orderRequest.put("orderType", "IN_STOCK");
        orderRequest.put("notes", "Хочу купить BMW X5");

        String orderResponse = mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(orderResponse).get("id").asText();

        mockMvc.perform(post("/api/manager/orders/{id}/assign", orderId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("status", "AWAITING_PAYMENT");
        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", testAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderId", orderId);
        paymentRequest.put("amount", 3500000.0);
        paymentRequest.put("method", "CARD");

        String paymentResponse = mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(paymentResponse).get("id").asText();

        Map<String, Object> processRequest = new HashMap<>();
        processRequest.put("transactionId", "TXN-" + UUID.randomUUID().toString().substring(0, 8));
        processRequest.put("success", true);

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/spare-parts/{id}", sparePartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4));

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/write-off", sparePartId)
                        .header("X-User-Id", testWarehouseAdminId)
                        .param("quantity", "1")
                        .param("reason", "Продано клиенту"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));

        updateRequest.put("status", "READY_FOR_PICKUP");
        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", testAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        updateRequest.put("status", "COMPLETED");
        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", testAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cars/{id}", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD"));

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/api/spare-parts/{id}", sparePartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Disabled("Requires order-service endpoints (/api/client/orders, /api/admin/orders, etc.) to be available")
    @Test
    void shouldAddSparePartToExistingOrder() throws Exception {
        String carRequest = """
        {
            "brand": "BMW",
            "model": "X5",
            "bodyType": "SEDAN",
            "color": "BLACK",
            "driveType": "FRONT",
            "engineFuelType": "PETROL",
            "enginePower": 249.0,
            "engineDisplacement": 2.0,
            "transmissionGears": 8,
            "transmissionType": "AUTOMATIC",
            "price": 2000000.00
        }
        """;

        String carResponse = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", testAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String testCarId = objectMapper.readTree(carResponse).get("id").asText();

        mockMvc.perform(put("/api/admin/cars/{id}", testCarId)
                        .header("X-User-Id", testAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"AVAILABLE\"}"))
                .andExpect(status().isOk());

        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("carId", testCarId);
        orderRequest.put("orderType", "IN_STOCK");

        String orderResponse = mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(orderResponse).get("id").asText();

        mockMvc.perform(post("/api/manager/orders/{id}/assign", orderId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("status", "AWAITING_PAYMENT");
        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", testAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        String sparePartId = createSparePart("Oil Filter", "OIL_FILTER", 1500.0, 20);

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderId", orderId);
        paymentRequest.put("amount", 2000000.0);
        paymentRequest.put("method", "CARD");

        String paymentResponse = mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(paymentResponse).get("id").asText();

        Map<String, Object> processRequest = new HashMap<>();
        processRequest.put("transactionId", "TXN-" + UUID.randomUUID().toString().substring(0, 8));
        processRequest.put("success", true);

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/spare-parts/{id}", sparePartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(20));
    }
}