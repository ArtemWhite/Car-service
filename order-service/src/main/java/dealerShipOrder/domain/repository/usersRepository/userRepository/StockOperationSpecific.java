package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.warehouseAdmin.StockOperation;

import java.time.LocalDateTime;
import java.util.List;

public interface StockOperationSpecific
{
    StockOperation saveStockOperation(StockOperation operation);
    List<StockOperation> findStockOperationsByAdminId(String adminId);
    List<StockOperation> findStockOperationsByDateRange(LocalDateTime start, LocalDateTime end);
    List<StockOperation> findStockOperationsByItemId(String itemId);
}
