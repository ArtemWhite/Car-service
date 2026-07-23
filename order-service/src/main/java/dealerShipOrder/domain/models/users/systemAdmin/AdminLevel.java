package dealerShipOrder.domain.models.users.systemAdmin;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum AdminLevel
{
    JUNIOR_ADMIN("Младший администратор", 1) {
        @Override
        public Set<SystemPermission> getDefaultPermissions() {
            return EnumSet.of(
                    SystemPermission.VIEW_CARS,
                    SystemPermission.VIEW_SPARE_PARTS,
                    SystemPermission.VIEW_ORDERS,
                    SystemPermission.VIEW_TEST_DRIVES
            );
        }
    },

    ADMIN("Администратор", 2) {
        @Override
        public Set<SystemPermission> getDefaultPermissions() {
            Set<SystemPermission> perms = EnumSet.allOf(SystemPermission.class);
            perms.remove(SystemPermission.MANAGE_PERMISSIONS);
            perms.remove(SystemPermission.SYSTEM_CONFIG);
            return perms;
        }
    },

    SUPER_ADMIN("Супер-администратор", 3) {
        @Override
        public Set<SystemPermission> getDefaultPermissions() {
            return EnumSet.allOf(SystemPermission.class);
        }
    };

    private final String displayName;
    private final int level;

    AdminLevel(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public abstract Set<SystemPermission> getDefaultPermissions();

}
