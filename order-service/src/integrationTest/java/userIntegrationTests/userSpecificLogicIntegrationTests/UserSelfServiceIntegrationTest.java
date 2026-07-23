package userIntegrationTests.userSpecificLogicIntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import userIntegrationTests.userMainIntegrationTests.UserBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class UserSelfServiceIntegrationTest extends UserBaseIntegrationTest {

    private String testClientId;
    private String testManagerId;

    @BeforeEach
    void setUp() {
        cleanUpUsers();

        testClientId = UUID.randomUUID().toString();
        createUser(testClientId, "CLIENT", "selfclient@test.com", "ACTIVE");
        createClient(testClientId);

        testManagerId = UUID.randomUUID().toString();
        createUser(testManagerId, "MANAGER", "selfmanager@test.com", "ACTIVE");
        createManager(testManagerId, "SALES_MANAGER");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldGetOwnProfile() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClientId))
                .andExpect(jsonPath("$.email").value("selfclient@test.com"))
                .andExpect(jsonPath("$.userType").value("CLIENT"));
    }

    @Test
    void shouldUpdateOwnFirstName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "UpdatedSelfName");

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedSelfName"));
    }

    @Test
    void shouldUpdateOwnEmail() throws Exception {
        String newEmail = "newselfemail@test.com";
        Map<String, Object> request = new HashMap<>();
        request.put("email", newEmail);

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(newEmail));
    }

    @Test
    void shouldUpdateOwnPhone() throws Exception {
        String newPhone = "+79998887766";
        Map<String, Object> request = new HashMap<>();
        request.put("phone", newPhone);

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value(newPhone));
    }

    @Test
    void shouldNotUpdateAnotherUserProfile() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "HackedName");

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldChangeOwnPassword() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("oldPassword", "hashed_" + testClientId.substring(0, 8));
        request.put("newPassword", "NewSecurePass456");

        mockMvc.perform(post("/api/users/me/password")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotChangeOwnPasswordWithWrongOld() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("oldPassword", "wrong_password");
        request.put("newPassword", "NewSecurePass456");

        mockMvc.perform(post("/api/users/me/password")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Old password is incorrect"));
    }

    @Test
    void shouldNotUpdateOwnUserType() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("userType", "MANAGER");

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("CLIENT"));
    }

    @Test
    void shouldNotUpdateOwnStatus() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("status", "BLOCKED");

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldClientSubscribeToNewsletter() throws Exception {
        mockMvc.perform(post("/api/client/me/newsletter/subscribe")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletterSubscribed").value(true));
    }

    @Test
    void shouldClientUnsubscribeFromNewsletter() throws Exception {
        jdbcTemplate.update(
                "UPDATE clients SET newsletter_subscribed = true WHERE user_id = ?::uuid",
                UUID.fromString(testClientId)
        );

        mockMvc.perform(post("/api/client/me/newsletter/unsubscribe")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletterSubscribed").value(false));
    }

    @Test
    void shouldClientSetPreferredContactMethod() throws Exception {
        mockMvc.perform(put("/api/client/me/contact-method")
                        .header("X-User-Id", testClientId)
                        .param("method", "phone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredContactMethod").value("phone"));
    }

    @Test
    void shouldManagerSetAvailability() throws Exception {
        mockMvc.perform(put("/api/manager/me/availability")
                        .header("X-User-Id", testManagerId)
                        .param("available", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldNotManagerPromoteHimself() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("position", "SENIOR_MANAGER");

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", testManagerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("SALES_MANAGER")); // Unchanged
    }

    @Test
    void shouldGetOwnOrdersAsClient() throws Exception {
        String orderId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO client_orders (client_id, order_id) VALUES (?::uuid, ?)",
                UUID.fromString(testClientId), orderId
        );

        mockMvc.perform(get("/api/client/me/orders")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetOwnTestDrivesAsClient() throws Exception {
        String testDriveId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO client_test_drives (client_id, test_drive_id) VALUES (?::uuid, ?)",
                UUID.fromString(testClientId), testDriveId
        );

        mockMvc.perform(get("/api/client/me/test-drives")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetOwnManagedSectionsAsWarehouseAdmin() throws Exception {
        String warehouseId = UUID.randomUUID().toString();
        createUser(warehouseId, "WAREHOUSE_ADMIN", "whself@test.com", "ACTIVE");
        createWarehouseAdmin(warehouseId, "WAREHOUSE_WORKER");
        assignWarehouseAdminToSection(warehouseId, "SEC-01");

        mockMvc.perform(get("/api/warehouse-admin/me/sections")
                        .header("X-User-Id", warehouseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionIds.length()").value(1))
                .andExpect(jsonPath("$.sectionIds[0]").value("SEC-01"));
    }

    @Test
    void shouldStartOwnShiftAsWarehouseAdmin() throws Exception {
        String warehouseId = UUID.randomUUID().toString();
        createUser(warehouseId, "WAREHOUSE_ADMIN", "whshift@test.com", "ACTIVE");
        createWarehouseAdmin(warehouseId, "WAREHOUSE_WORKER");

        mockMvc.perform(post("/api/warehouse-admin/me/shift/start")
                        .header("X-User-Id", warehouseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onDuty").value(true));
    }

    @Test
    void shouldEndOwnShiftAsWarehouseAdmin() throws Exception {
        String warehouseId = UUID.randomUUID().toString();
        createUser(warehouseId, "WAREHOUSE_ADMIN", "whend@test.com", "ACTIVE");
        createWarehouseAdmin(warehouseId, "WAREHOUSE_WORKER");

        jdbcTemplate.update(
                "UPDATE warehouse_admins SET on_duty = true WHERE user_id = ?::uuid",
                UUID.fromString(warehouseId)
        );

        mockMvc.perform(post("/api/warehouse-admin/me/shift/end")
                        .header("X-User-Id", warehouseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onDuty").value(false));
    }

    @Test
    void shouldGetOwnAuditLogAsAdmin() throws Exception {
        createAuditLogEntry(adminId, "TEST_ACTION", "Test audit entry");

        mockMvc.perform(get("/api/admin/me/audit-log")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operations.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldUpdateOwnProfileWithMultipleFields() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Multi");
        request.put("lastName", "Update");
        request.put("middleName", "Test");
        request.put("phone", "+79991112233");

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Multi"))
                .andExpect(jsonPath("$.lastName").value("Update"))
                .andExpect(jsonPath("$.middleName").value("Test"))
                .andExpect(jsonPath("$.phone").value("+79991112233"));
    }

    @Test
    void shouldNotUpdateOwnProfileWithInvalidEmail() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("email", "invalid-email");

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}