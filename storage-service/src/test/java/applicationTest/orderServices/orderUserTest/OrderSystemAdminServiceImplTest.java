package applicationTest.orderServices.orderUserTest;

import application.dtos.request.orderRequest.UpdateOrderRequest;
import application.dtos.response.orderResponse.OrderResponse;
import application.mapper.OrderMapper;
import application.services.orderService.systemAdmin.OrderSystemAdminServiceImpl;
import domain.exception.DomainValidationException;
import domain.exception.EntityNotFoundException;
import domain.models.order.Order;
import domain.models.order.OrderStatus;
import domain.models.users.systemAdmin.SystemAdmin;
import domain.models.users.systemAdmin.AdminLevel;
import domain.models.users.systemAdmin.SystemPermission;
import domain.repository.orderRepository.OrderRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderSystemAdminService Tests")
class OrderSystemAdminServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderSystemAdminServiceImpl orderAdminService;

    private SystemAdmin admin;
    private Order order;
    private UpdateOrderRequest updateRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        admin = new SystemAdmin("Admin", "User", null, "admin@email.com", "+123", "pass", "emp1", AdminLevel.ADMIN);
        order = Order.createInStockOrder("order123", "client123", "car123");
        orderResponse = new OrderResponse();

        updateRequest = new UpdateOrderRequest();
        updateRequest.setStatus("PAID");
        updateRequest.setNotes("Updated notes");
    }

    @Test
    @DisplayName("Should update order status successfully")
    void shouldUpdateOrderStatusSuccessfully() {
        Order realOrder = Order.createInStockOrder("order123", "client123", "car123");
        realOrder.assignManager("manager123");
        realOrder.awaitPayment();
        System.out.println("1. Before: " + realOrder.getStatus());

        OrderMapper realMapper = new OrderMapper(null);

        realMapper.updateOrderStatus(realOrder, "PAID", null);

        System.out.println("2. After mapper: " + realOrder.getStatus());

        assertEquals(OrderStatus.PAID, realOrder.getStatus());
    }

    @Test
    @DisplayName("Should log action when updating order")
    void shouldLogActionWhenUpdatingOrder() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        orderAdminService.updateOrder("order123", updateRequest);

        assertFalse(admin.getAuditLog().isEmpty());
        assertEquals("UPDATE_ORDER", admin.getAuditLog().get(0).getAction());
    }

    @Test
    @DisplayName("Should throw exception when admin not found")
    void shouldThrowExceptionWhenAdminNotFound() {
        when(userRepository.findById("admin999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            orderAdminService.updateOrder("order123", updateRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(orderRepository.findById("order999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            orderAdminService.updateOrder("order999", updateRequest);
        });
    }

    @Test
    @DisplayName("Should delete inactive order successfully")
    void shouldDeleteInactiveOrderSuccessfully() {
        Order cancelledOrder = Order.createInStockOrder("order123", "client123", "car123");
        cancelledOrder.cancel("Test");

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(cancelledOrder));
        doNothing().when(orderRepository).delete("order123");

        orderAdminService.deleteOrder("order123", "Cleanup");

        verify(orderRepository, times(1)).delete("order123");
    }

    @Test
    @DisplayName("Should log action when deleting order")
    void shouldLogActionWhenDeletingOrder() {
        Order cancelledOrder = Order.createInStockOrder("order123", "client123", "car123");
        cancelledOrder.cancel("Test");

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(cancelledOrder));
        doNothing().when(orderRepository).delete("order123");

        orderAdminService.deleteOrder("order123", "Cleanup");

        assertFalse(admin.getAuditLog().isEmpty());
        assertEquals("DELETE_ORDER", admin.getAuditLog().get(0).getAction());

        assertTrue(admin.getAuditLog().get(0).getDetails().contains("order123"));

        assertTrue(admin.getAuditLog().get(0).getDetails().startsWith("Deleted order:"));
    }

    @Test
    @DisplayName("Should throw exception when deleting active order")
    void shouldThrowExceptionWhenDeletingActiveOrder() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));

        assertThrows(DomainValidationException.class, () -> {
            orderAdminService.deleteOrder("order123", "Reason");
        });
    }

    @Test
    @DisplayName("Should get all orders for admin successfully")
    void shouldGetAllOrdersForAdminSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderAdminService.getAllOrdersForAdmin();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should throw exception when admin has no permission")
    void shouldThrowExceptionWhenAdminHasNoPermission() {
        SystemAdmin noPermissionAdmin = new SystemAdmin(
                "NoPerm", "Admin", null, "noperm@email.com", "+123", "pass", "emp3", AdminLevel.JUNIOR_ADMIN
        );
        noPermissionAdmin.removePermission(SystemPermission.VIEW_ORDERS);

        when(userRepository.findById("admin123")).thenReturn(Optional.of(noPermissionAdmin));

        assertThrows(DomainValidationException.class, () -> {
            orderAdminService.getAllOrdersForAdmin();
        });
    }
}