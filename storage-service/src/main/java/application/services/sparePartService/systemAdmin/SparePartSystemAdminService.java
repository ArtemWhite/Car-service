package application.services.sparePartService.systemAdmin;

import application.dtos.request.spareRequest.CreateSparePartRequest;
import application.dtos.request.spareRequest.UpdateSparePartRequest;
import application.dtos.response.spareResponse.SparePartResponse;

public interface SparePartSystemAdminService {
    SparePartResponse createSparePart(CreateSparePartRequest request);
    SparePartResponse updateSparePart(String id, UpdateSparePartRequest request);
    void deleteSparePart(String id, String reason);
    void addCompatibleModel(String sparePartId, String modelId);
    void removeCompatibleModel(String sparePartId, String modelId);
    SparePartResponse getSparePartById(String id);
}