package application.services.sparePartService.manager;

import application.dtos.response.spareResponse.SparePartResponse;

import java.util.List;

public interface SparePartManagerService {
    List<SparePartResponse> getLowStockParts(int threshold);
    List<SparePartResponse> getOutOfStockParts();
    void requestRestock(String sparePartId, int quantity);
}