package application.mapper;

import application.dtos.request.spareRequest.CreateSparePartRequest;
import application.dtos.request.spareRequest.UpdateSparePartRequest;
import application.dtos.response.spareResponse.SparePartResponse;
import domain.exception.DomainValidationException;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SparePartMapper {

    public SparePart toDomain(CreateSparePartRequest request, Set<CarModel> compatibleModels) {
        return SparePart.builder()
                .type(SpareType.valueOf(request.getSpareType()))
                .name(request.getName())
                .description(request.getDescription())
                .price(Price.of(request.getPrice(), request.getCurrency() != null ? request.getCurrency() : "RUB"))
                .compatibles(compatibleModels)
                .manufacturer(request.getManufacturer())
                .partNumber(request.getPartNumber())
                .build();
    }

    public SparePartResponse toResponse(SparePart sparePart, int quantity, String sectionId, String location) {
        SparePartResponse response = new SparePartResponse();

        response.setId(sparePart.getId());
        response.setSpareType(sparePart.getType().name());
        response.setSpareTypeDisplayName(sparePart.getType().getDisplayName());
        response.setName(sparePart.getName());
        response.setDescription(sparePart.getDescription());

        response.setManufacturer(extractManufacturer(sparePart));
        response.setPartNumber(extractPartNumber(sparePart));

        response.setPrice(sparePart.getPrice().getAmount().doubleValue());
        response.setPriceFormatted(formatPrice(sparePart.getPrice()));

        response.setQuantity(quantity);
        response.setSectionId(sectionId);
        response.setLocation(location);

        response.setCompatibleModelsCount(sparePart.getCompatibles().size());

        response.setLastUpdated(java.time.LocalDateTime.now());

        updateStockStatus(response, quantity);

        return response;
    }

    public SparePartResponse toResponse(SparePart sparePart) {
        return toResponse(sparePart, 0, null, null);
    }

    public List<SparePartResponse> toResponseList(List<SparePart> spareParts) {
        return spareParts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SparePartResponse> toResponseList(List<SparePart> spareParts,
                                                  java.util.Map<String, Integer> stockMap,
                                                  java.util.Map<String, String> sectionMap,
                                                  java.util.Map<String, String> locationMap) {
        return spareParts.stream()
                .map(sp -> toResponse(
                        sp,
                        stockMap.getOrDefault(sp.getId(), 0),
                        sectionMap.get(sp.getId()),
                        locationMap.get(sp.getId())
                ))
                .collect(Collectors.toList());
    }

    private void updateStockStatus(SparePartResponse response, int quantity) {
        response.setQuantity(quantity);

        if (quantity <= 0) {
            response.setStatus("OUT_OF_STOCK");
            response.setStatusDisplayName("Нет в наличии");
            response.setInStock(false);
            response.setOutOfStock(true);
            response.setLowStock(false);
        } else if (quantity < 5) {
            response.setStatus("LOW_STOCK");
            response.setStatusDisplayName("Мало (менее 5 шт.)");
            response.setInStock(true);
            response.setOutOfStock(false);
            response.setLowStock(true);
        } else {
            response.setStatus("IN_STOCK");
            response.setStatusDisplayName("В наличии");
            response.setInStock(true);
            response.setOutOfStock(false);
            response.setLowStock(false);
        }
    }

    public void updateDomain(SparePart sparePart, UpdateSparePartRequest request, Set<CarModel> newCompatibleModels) {
        if (request.getName() != null && !request.getName().isBlank()) {
            sparePart.setName(request.getName());
        }

        if (request.getDescription() != null) {
            sparePart.setDescription(request.getDescription());
        }

        if (request.getSpareType() != null && !request.getSpareType().isBlank()) {
            try {
                SpareType type = SpareType.valueOf(request.getSpareType());
                sparePart.setType(type);
            } catch (IllegalArgumentException e) {
                throw new DomainValidationException("Invalid spare type: " + request.getSpareType());
            }
        }

        if (request.getPrice() != null && request.getPrice() > 0) {
            String currency = sparePart.getPrice() != null ?
                    sparePart.getPrice().getCurrency().getCurrencyCode() : "RUB";
            sparePart.setPrice(Price.of(request.getPrice(), currency));
        }

        if (newCompatibleModels != null) {
            sparePart.setCompatibles(newCompatibleModels);
        }
    }
    private String formatPrice(Price price) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
        format.setCurrency(price.getCurrency());
        return format.format(price.getAmount())
                .replace(',', ' ')
                .replace("\u00A0", " ");
    }

    private String extractManufacturer(SparePart sparePart) {
        String name = sparePart.getName().toLowerCase();
        if (name.contains("bosch")) return "Bosch";
        if (name.contains("mann")) return "Mann-Filter";
        if (name.contains("febi")) return "Febi Bilstein";
        if (name.contains("ngk")) return "NGK";
        if (name.contains("continental")) return "Continental";
        if (name.contains("valeo")) return "Valeo";
        return "Неизвестно";
    }

    private String extractPartNumber(SparePart sparePart) {
        return "PN-" + sparePart.getId().substring(0, Math.min(8, sparePart.getId().length()));
    }
}