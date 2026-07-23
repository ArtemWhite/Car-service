package application.services.sparePartService;

import application.dtos.request.spareRequest.SparePartFilterRequest;
import application.dtos.request.spareRequest.UpdateSparePartRequest;
import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import domain.repository.carRepository.CarRepository;
import domain.repository.sparePartRepository.SparePartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SparePartServiceImpl extends BaseSparePartService implements SparePartService {

    public SparePartServiceImpl(
            SparePartRepository sparePartRepository,
            CarRepository carRepository,
            SparePartMapper sparePartMapper) {
        super(sparePartRepository, carRepository, sparePartMapper);
    }

    public void updateDomain(SparePart sparePart, UpdateSparePartRequest request, Set<CarModel> newCompatibleModels) {
        if (request.getName() != null && !request.getName().isBlank()) {
            sparePart.setName(request.getName());
        }

        if (request.getDescription() != null) {
            sparePart.setDescription(request.getDescription());
        }

        if (request.getSpareType() != null && !request.getSpareType().isBlank()) {
            sparePart.setType(SpareType.valueOf(request.getSpareType()));
        }

        if (request.getPrice() != null && request.getPrice() > 0) {
            sparePart.setPrice(Price.of(request.getPrice(), "RUB"));
        }

        if (newCompatibleModels != null) {
            sparePart.setCompatibles(newCompatibleModels);
        }
    }

    @Override
    public SparePartResponse getSparePartById(String id) {
        SparePart sparePart = findSparePartById(id);
        int quantity = sparePartRepository.getStockQuantity(id);
        return sparePartMapper.toResponse(sparePart, quantity, null, null);
    }

    @Override
    public List<SparePartResponse> getAllSpareParts() {
        List<SparePart> spareParts = sparePartRepository.findAll();
        return spareParts.stream()
                .map(part -> {
                    int quantity = sparePartRepository.getStockQuantity(part.getId());
                    return sparePartMapper.toResponse(part, quantity, null, null);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SparePartResponse> getSparePartsWithFilters(SparePartFilterRequest filter) {
        List<SparePart> allParts = sparePartRepository.findAll();

        List<SparePart> filteredParts = allParts.stream()
                .filter(part -> filterByType(part, filter.getSpareType()))
                .filter(part -> filterByManufacturer(part, filter.getManufacturer()))
                .filter(part -> filterByPrice(part, filter.getMinPrice(), filter.getMaxPrice()))
                .filter(part -> filterByCompatibleModel(part, filter.getCompatibleModelId()))
                .filter(part -> filterByStockStatus(part, filter.getInStock(), filter.getLowStock()))
                .filter(part -> filterBySearchQuery(part, filter.getSearchQuery()))
                .toList();

        List<SparePartResponse> responses = filteredParts.stream()
                .map(part -> {
                    int quantity = sparePartRepository.getStockQuantity(part.getId());
                    return sparePartMapper.toResponse(part, quantity, null, null);
                })
                .collect(Collectors.toList());

        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "name";
        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "ASC";
        boolean isAsc = "ASC".equalsIgnoreCase(sortDirection);

        Comparator<SparePartResponse> comparator = switch (sortBy.toLowerCase()) {
            case "price" -> Comparator.comparing(SparePartResponse::getPrice, Comparator.nullsLast(Double::compareTo));
            case "quantity" -> Comparator.comparing(SparePartResponse::getQuantity, Comparator.nullsLast(Integer::compareTo));
            default -> Comparator.comparing(SparePartResponse::getName, Comparator.nullsLast(String::compareTo));
        };

        if (!isAsc) {
            comparator = comparator.reversed();
        }

        responses.sort(comparator);

        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;

        int start = page * size;
        int end = Math.min(start + size, responses.size());

        if (start >= responses.size()) {
            return new ArrayList<>();
        }

        return responses.subList(start, end);
    }

    private boolean filterByManufacturer(SparePart part, String manufacturer) {
        if (manufacturer == null || manufacturer.isBlank()) {
            return true;
        }
        String partManufacturer = part.getManufacturer();
        if (partManufacturer == null) {
            return false;
        }
        return partManufacturer.equalsIgnoreCase(manufacturer);
    }

    @Override
    public List<SparePartResponse> getSparePartsByType(String spareType) {
        SpareType type;
        try {
            type = SpareType.valueOf(spareType);
        } catch (IllegalArgumentException e) {
            return List.of();
        }

        List<SparePart> spareParts = sparePartRepository.findByType(type);
        return spareParts.stream()
                .map(part -> {
                    int quantity = sparePartRepository.getStockQuantity(part.getId());
                    return sparePartMapper.toResponse(part, quantity, null, null);
                })
                .collect(Collectors.toList());
    }

    private boolean filterByType(SparePart part, String spareType) {
        if (spareType == null || spareType.isBlank()) {
            return true;
        }
        try {
            SpareType type = SpareType.valueOf(spareType);
            return part.getType() == type;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean filterByPrice(SparePart part, Double minPrice, Double maxPrice) {
        double price = part.getPrice().getAmount().doubleValue();
        if (minPrice != null && price < minPrice) {
            return false;
        }
        if (maxPrice != null && price > maxPrice) {
            return false;
        }
        return true;
    }

    private boolean filterByCompatibleModel(SparePart part, String compatibleModelId) {
        if (compatibleModelId == null || compatibleModelId.isBlank()) {
            return true;
        }
        CarModel model = carRepository.findModelById(compatibleModelId).orElse(null);
        if (model == null) {
            return false;
        }
        return part.isCompatibleWith(model);
    }

    private boolean filterByStockStatus(SparePart part, Boolean inStock, Boolean lowStock) {
        int quantity = sparePartRepository.getStockQuantity(part.getId());
        if (inStock != null && inStock) {
            return quantity > 0;
        }
        if (lowStock != null && lowStock) {
            int threshold = 5;
            return quantity > 0 && quantity < threshold;
        }
        return true;
    }

    private boolean filterBySearchQuery(SparePart part, String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return true;
        }
        String query = searchQuery.toLowerCase();
        return part.getName().toLowerCase().contains(query) ||
                part.getDescription().toLowerCase().contains(query) ||
                part.getType().getDisplayName().toLowerCase().contains(query);
    }
}