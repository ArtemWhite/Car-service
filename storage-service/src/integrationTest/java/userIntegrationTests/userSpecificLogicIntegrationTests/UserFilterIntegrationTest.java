package userIntegrationTests.userSpecificLogicIntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import userIntegrationTests.userMainIntegrationTests.UserBaseIntegrationTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserFilterIntegrationTest extends UserBaseIntegrationTest {

    @BeforeEach
    void setUp() {
        cleanUpUsers();
        createTestUsers();

        for (int i = 1; i <= 5; i++) {
            String userId = UUID.randomUUID().toString();
            createUser(userId, "CLIENT", "filter" + i + "@test.com", "ACTIVE");
            createClient(userId);
        }

        String seniorManagerId = UUID.randomUUID().toString();
        createUser(seniorManagerId, "MANAGER", "senior@test.com", "ACTIVE");
        createManager(seniorManagerId, "SENIOR_MANAGER");

        String leadManagerId = UUID.randomUUID().toString();
        createUser(leadManagerId, "MANAGER", "lead@test.com", "ACTIVE");
        createManager(leadManagerId, "LEAD_MANAGER");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFilterByUserType() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("userType", "CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[*].userType").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("CLIENT"))));
    }

    @Test
    void shouldFilterByEmail() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("email", "client@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(1))
                .andExpect(jsonPath("$.users[0].email").value("client@test.com"));
    }

    @Test
    void shouldFilterByPhone() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("phone", "+71234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldFilterByFirstName() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("firstName", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldFilterByLastName() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldFilterByActiveFlag() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[*].status").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("ACTIVE"))));
    }

    @Test
    void shouldFilterByManagerPosition() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("managerPosition", "SENIOR_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[*].position").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("SENIOR_MANAGER"))));
    }

    @Test
    void shouldFilterByAdminLevel() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("adminLevel", "SUPER_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].adminLevel").value("SUPER_ADMIN"));
    }

    @Test
    void shouldCombineMultipleFilters() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("userType", "CLIENT")
                        .param("status", "ACTIVE")
                        .param("firstName", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray());
    }

    @Test
    void shouldSearchByFullName() throws Exception {
        mockMvc.perform(get("/api/admin/users/search")
                        .header("X-User-Id", adminId)
                        .param("query", "Test User"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldSearchByEmailPartial() throws Exception {
        mockMvc.perform(get("/api/admin/users/search")
                        .header("X-User-Id", adminId)
                        .param("query", "client"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("email", "nonexistent@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(0));
    }

    @Test
    void shouldFilterByDateRange() throws Exception {
        String from = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String to = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("registeredFrom", from)
                        .param("registeredTo", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray());
    }

    @Test
    void shouldFilterByLastActiveBefore() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("lastActiveBefore", LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFilterByRoleAndStatus() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("userType", "MANAGER")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[*].userType").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("MANAGER"))))
                .andExpect(jsonPath("$.users[*].status").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("ACTIVE"))));
    }

    @Test
    void shouldFilterByMultipleManagerProperties() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("userType", "MANAGER")
                        .param("managerPosition", "SALES_MANAGER")
                        .param("available", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFilterByMultipleAdminProperties() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("userType", "SYSTEM_ADMIN")
                        .param("adminLevel", "SUPER_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPaginateFilterResults() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(3)));
    }

    @Test
    void shouldSortFilterResults() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("sortBy", "registeredAt")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFilterWarehouseAdminsBySection() throws Exception {
        String sectionId = "SEC-FILTER-001";
        assignWarehouseAdminToSection(warehouseAdminId, sectionId);

        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("userType", "WAREHOUSE_ADMIN")
                        .param("sectionId", sectionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(1));
    }

    @Test
    void shouldFilterClientsByNewsletter() throws Exception {
        jdbcTemplate.update(
                "UPDATE clients SET newsletter_subscribed = true WHERE user_id = ?::uuid",
                UUID.fromString(clientId)
        );

        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("userType", "CLIENT")
                        .param("newsletterSubscribed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(1));
    }

    @Test
    void shouldFilterManagersByAvailability() throws Exception {
        jdbcTemplate.update(
                "UPDATE managers SET available = false WHERE user_id = ?::uuid",
                UUID.fromString(managerId)
        );

        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("userType", "MANAGER")
                        .param("available", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(1));
    }
}