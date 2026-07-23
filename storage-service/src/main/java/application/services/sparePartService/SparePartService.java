package application.services.sparePartService;

import application.dtos.request.spareRequest.SparePartFilterRequest;
import application.dtos.response.spareResponse.SparePartResponse;

import java.util.List;

public interface SparePartService {
    SparePartResponse getSparePartById(String id);
    List<SparePartResponse> getAllSpareParts();
    List<SparePartResponse> getSparePartsWithFilters(SparePartFilterRequest filter);
    List<SparePartResponse> getSparePartsByType(String spareType);
}