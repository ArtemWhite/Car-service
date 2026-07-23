package userIntegrationTests;

import org.example.AutoDealerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AutoDealerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    // Токен client1 (sub = 51d078dd-555c-456c-a2e2-72546680a066)
    private final String CLIENT1_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnSHhHMTJ5b1pKbUVnZU9Tc3VndTNRSjdzbS03cFd4Z2FrdzliejVVOV9jIn0.eyJzdWIiOiI1MWQwNzhkZC01NTVjLTQ1NmMtYTJlMi03MjU0NjY4MGEwNjYiLCJlbWFpbCI6ImNsaWVudEBkZWFsZXJzaGlwLmNvbSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJDTElFTlQiXX0sInByZWZlcnJlZF91c2VybmFtZSI6ImNsaWVudDEifQ.xxx";

    // Токен testclient (sub = 57b055cc-ea91-4ffe-8bf2-657f20dd5f22)
    private final String CLIENT2_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnSHhHMTJ5b1pKbUVnZU9Tc3VndTNRSjdzbS03cFd4Z2FrdzliejVVOV9jIn0.eyJzdWIiOiI1N2IwNTVjYy1lYTkxLTRmZmUtOGJmMi02NTdmMjBkZDVmMjIiLCJlbWFpbCI6Ind3QG1haS5jb20iLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiQ0xJRU5UIl19LCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ0ZXN0Y2xpZXQifQ.xxx";

    @Test
    void testClientSeesOnlyHisOwnOrders() throws Exception {
        String client1OrderId = createOrder(CLIENT1_TOKEN, "dc6f5830-7e0d-43e0-9823-535a9f0dc567");
        String client2OrderId = createOrder(CLIENT2_TOKEN, "dc6f5830-7e0d-43e0-9823-535a9f0dc567");

        mockMvc.perform(get("/api/client/orders/my")
                        .header("Authorization", "Bearer " + CLIENT1_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[*].id").value(org.hamcrest.Matchers.hasItem(client1OrderId)))
                .andExpect(jsonPath("$.orders[*].id").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(client2OrderId))));

        mockMvc.perform(get("/api/client/orders/my")
                        .header("Authorization", "Bearer " + CLIENT2_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[*].id").value(org.hamcrest.Matchers.hasItem(client2OrderId)))
                .andExpect(jsonPath("$.orders[*].id").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(client1OrderId))));
    }

    @Test
    void testClientCannotAccessOtherClientsOrderById() throws Exception {
        String client1OrderId = createOrder(CLIENT1_TOKEN, "dc6f5830-7e0d-43e0-9823-535a9f0dc567");

        mockMvc.perform(get("/api/client/orders/{id}", client1OrderId)
                        .header("Authorization", "Bearer " + CLIENT2_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void testClientCannotCancelOtherClientsOrder() throws Exception {
        String client1OrderId = createOrder(CLIENT1_TOKEN, "dc6f5830-7e0d-43e0-9823-535a9f0dc567");

        mockMvc.perform(post("/api/client/orders/{id}/cancel", client1OrderId)
                        .header("Authorization", "Bearer " + CLIENT2_TOKEN)
                        .param("reason", "Не хочу"))
                .andExpect(status().isForbidden());
    }

    private String createOrder(String token, String carId) throws Exception {
        String response = mockMvc.perform(post("/api/client/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carId\": \"" + carId + "\", \"deliveryAddress\": \"Test Address\", \"deliveryType\": \"DELIVERY\", \"paymentMethod\": \"CARD\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int start = response.indexOf("\"id\":\"") + 6;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}