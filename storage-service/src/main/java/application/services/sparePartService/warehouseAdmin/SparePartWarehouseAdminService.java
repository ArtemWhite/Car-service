package application.services.sparePartService.warehouseAdmin;

import application.dtos.request.spareRequest.UpdateStockRequest;
import application.dtos.response.spareResponse.SparePartResponse;

public interface SparePartWarehouseAdminService {
    SparePartResponse updateStock(UpdateStockRequest request);
    SparePartResponse receiveShipment(String sparePartId, int quantity);
    SparePartResponse moveToLocation(String sparePartId, String section, String location);
    SparePartResponse writeOff(String sparePartId, int quantity, String reason);
}