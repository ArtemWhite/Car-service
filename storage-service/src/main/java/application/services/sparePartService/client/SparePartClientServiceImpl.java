package application.services.sparePartService.client;

import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import application.services.sparePartService.BaseSparePartService;
import domain.exception.EntityNotFoundException;
import domain.models.car.CarModel;
import domain.models.sparePart.SparePart;
import domain.repository.carRepository.CarRepository;
import domain.repository.sparePartRepository.SparePartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SparePartClientServiceImpl extends BaseSparePartService implements SparePartClientService {

    public SparePartClientServiceImpl(
            SparePartRepository sparePartRepository,
            CarRepository carRepository,
            SparePartMapper sparePartMapper) {
        super(sparePartRepository, carRepository, sparePartMapper);
    }

    @Override
    public List<SparePartResponse> findCompatibleSpareParts(String carModelId) {
        log.debug("Finding compatible spare parts for car model: {}", carModelId);

        CarModel carModel = carRepository.findModelById(carModelId)
                .orElseThrow(() -> new EntityNotFoundException("Car model not found: " + carModelId));

        Map<String, Integer> stockMap = new HashMap<>();
        Map<String, String> sectionMap = new HashMap<>();
        Map<String, String> locationMap = new HashMap<>();

        List<SparePart> compatibleParts = sparePartRepository.findByCompatibleModelWithStock(
                carModel, stockMap, sectionMap, locationMap);

        return compatibleParts.stream()
                .map(part -> sparePartMapper.toResponse(
                        part,
                        stockMap.getOrDefault(part.getId(), 0),
                        sectionMap.get(part.getId()),
                        locationMap.get(part.getId())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public SparePartResponse getSparePartDetails(String id) {
        log.debug("Getting spare part details for id: {}", id);

        SparePart sparePart = findSparePartById(id);
        int quantity = sparePartRepository.getStockQuantity(id);
        String sectionId = sparePartRepository.getSectionId(id);
        String location = sparePartRepository.getLocation(id);

        return sparePartMapper.toResponse(sparePart, quantity, sectionId, location);
    }

    @Override
    public List<SparePartResponse> searchSpareParts(String query) {
        log.debug("Searching spare parts by query: {}", query);

        List<SparePart> parts = sparePartRepository.findByNameContaining(query);
        return parts.stream()
                .map(part -> {
                    int quantity = sparePartRepository.getStockQuantity(part.getId());
                    return sparePartMapper.toResponse(part, quantity, null, null);
                })
                .collect(Collectors.toList());
    }
}