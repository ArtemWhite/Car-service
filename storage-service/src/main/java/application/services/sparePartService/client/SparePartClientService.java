package application.services.sparePartService.client;

import application.dtos.response.spareResponse.SparePartResponse;

import java.util.List;

public interface SparePartClientService {
    List<SparePartResponse> findCompatibleSpareParts(String carModelId);
    SparePartResponse getSparePartDetails(String id);
    List<SparePartResponse> searchSpareParts(String query);
}