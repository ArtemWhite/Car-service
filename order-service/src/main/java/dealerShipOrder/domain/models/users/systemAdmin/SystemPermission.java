package dealerShipOrder.domain.models.users.systemAdmin;

public enum SystemPermission
{
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    BLOCK_USER,
    VIEW_USERS,

    MANAGE_CARS,
    VIEW_CARS,

    MANAGE_SPARE_PARTS,
    VIEW_SPARE_PARTS,

    VIEW_ORDERS,
    UPDATE_ORDER,
    CANCEL_ORDER,

    VIEW_TEST_DRIVES,
    UPDATE_TEST_DRIVE,

    MANAGE_PERMISSIONS,
    VIEW_AUDIT_LOG,
    SYSTEM_CONFIG
}
