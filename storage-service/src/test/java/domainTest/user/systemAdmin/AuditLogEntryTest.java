package domainTest.user.systemAdmin;

import domain.models.users.systemAdmin.AuditLogEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuditLogEntry Tests")
class AuditLogEntryTest {

    @Test
    @DisplayName("Should create audit log entry with all fields")
    void shouldCreateAuditLogEntry() {
        String adminId = "admin123";
        String action = "CREATE_USER";
        String details = "Created user with id: user456";
        LocalDateTime timestamp = LocalDateTime.now();

        AuditLogEntry entry = new AuditLogEntry(adminId, action, details, timestamp);

        assertEquals(adminId, entry.getAdminId());
        assertEquals(action, entry.getAction());
        assertEquals(details, entry.getDetails());
        assertEquals(timestamp, entry.getTimestamp());
    }

    @Test
    @DisplayName("Should allow null fields")
    void shouldAllowNullFields() {
        AuditLogEntry entry = new AuditLogEntry(null, null, null, null);

        assertNull(entry.getAdminId());
        assertNull(entry.getAction());
        assertNull(entry.getDetails());
        assertNull(entry.getTimestamp());
    }
}