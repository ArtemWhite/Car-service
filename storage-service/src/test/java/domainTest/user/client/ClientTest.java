package domainTest.user.client;

import domain.exception.DomainValidationException;
import domain.models.users.client.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Client Tests")
class ClientTest {

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client("emp123", "John", "Doe", "Michael", "john@email.com", "+1234567890", "password123");
    }

    @Test
    @DisplayName("Should create client with all fields")
    void shouldCreateClient() {
        assertNotNull(client);
        assertEquals("emp123", client.getId());
        assertEquals("John", client.getFirstName());
        assertEquals("Doe", client.getLastName());
        assertEquals("Michael", client.getMiddleName());
        assertEquals("john@email.com", client.getEmail());
        assertEquals("+1234567890", client.getPhone());
        assertEquals("email", client.getPreferredContactMethod());
        assertFalse(client.isNewsletterSubscribed());
        assertNotNull(client.getOrderHistory());
        assertNotNull(client.getTestDriveRequests());
        assertEquals(0, client.getOrderCount());
    }

    @Test
    @DisplayName("Should initialize empty lists on creation")
    void shouldInitializeEmptyLists() {
        assertTrue(client.getOrderHistory().isEmpty());
        assertTrue(client.getTestDriveRequests().isEmpty());
        assertEquals(0, client.getOrderCount());
    }

    @Test
    @DisplayName("Should add order successfully")
    void shouldAddOrder() {
        client.addOrder("order123");

        assertEquals(1, client.getOrderCount());
        assertTrue(client.getOrderHistory().contains("order123"));
    }

    @Test
    @DisplayName("Should throw when adding order with null ID")
    void shouldThrowWhenAddingOrderWithNullId() {
        assertThrows(DomainValidationException.class, () -> {
            client.addOrder(null);
        });
    }

    @Test
    @DisplayName("Should throw when adding order with blank ID")
    void shouldThrowWhenAddingOrderWithBlankId() {
        assertThrows(DomainValidationException.class, () -> {
            client.addOrder("");
        });

        assertThrows(DomainValidationException.class, () -> {
            client.addOrder("   ");
        });
    }

    @Test
    @DisplayName("Should return unmodifiable order history")
    void shouldReturnUnmodifiableOrderHistory() {
        client.addOrder("order1");
        client.addOrder("order2");

        assertThrows(UnsupportedOperationException.class, () -> {
            client.getOrderHistory().add("order3");
        });
    }

    @Test
    @DisplayName("Should add test drive request")
    void shouldAddTestDriveRequest() {
        client.addTestDriveRequest("testDrive123");

        assertEquals(1, client.getTestDriveRequests().size());
        assertTrue(client.getTestDriveRequests().contains("testDrive123"));
        assertTrue(client.hasActiveTestDrive());
    }

    @Test
    @DisplayName("Should throw when adding test drive with null ID")
    void shouldThrowWhenAddingTestDriveWithNullId() {
        assertThrows(DomainValidationException.class, () -> {
            client.addTestDriveRequest(null);
        });
    }

    @Test
    @DisplayName("Should return unmodifiable test drive list")
    void shouldReturnUnmodifiableTestDriveList() {
        client.addTestDriveRequest("td1");

        assertThrows(UnsupportedOperationException.class, () -> {
            client.getTestDriveRequests().add("td2");
        });
    }

    @Test
    @DisplayName("Should set preferred contact method to email")
    void shouldSetPreferredContactMethodToEmail() {
        client.setPreferredContactMethod("email");
        assertEquals("email", client.getPreferredContactMethod());
    }

    @Test
    @DisplayName("Should set preferred contact method to phone")
    void shouldSetPreferredContactMethodToPhone() {
        client.setPreferredContactMethod("phone");
        assertEquals("phone", client.getPreferredContactMethod());
    }

    @Test
    @DisplayName("Should throw when setting invalid contact method")
    void shouldThrowWhenSettingInvalidContactMethod() {
        assertThrows(DomainValidationException.class, () -> {
            client.setPreferredContactMethod("invalid");
        });

        assertThrows(DomainValidationException.class, () -> {
            client.setPreferredContactMethod(null);
        });
    }

    @Test
    @DisplayName("Should subscribe to newsletter")
    void shouldSubscribeToNewsletter() {
        assertFalse(client.isNewsletterSubscribed());

        client.subscribeToNewsletter();
        assertTrue(client.isNewsletterSubscribed());
    }

    @Test
    @DisplayName("Should unsubscribe from newsletter")
    void shouldUnsubscribeFromNewsletter() {
        client.subscribeToNewsletter();
        assertTrue(client.isNewsletterSubscribed());

        client.unsubscribeFromNewsletter();
        assertFalse(client.isNewsletterSubscribed());
    }
}