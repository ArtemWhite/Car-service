package presentation.mappers;

import application.dtos.request.spareRequest.CreateSparePartRequest;
import application.dtos.request.spareRequest.UpdateSparePartRequest;
import application.dtos.request.spareRequest.SparePartFilterRequest;
import application.dtos.request.spareRequest.UpdateStockRequest;
import application.dtos.response.spareResponse.CompatibleModelDto;
import application.dtos.response.spareResponse.SparePartResponse;
import application.dtos.response.spareResponse.SparePartListResponse;
import application.dtos.response.spareResponse.SparePartCompatibilityResponse;

import presentation.dtos.request.spareRequestPresentationDto.SparePartCreatePresentationRequest;
import presentation.dtos.request.spareRequestPresentationDto.SparePartUpdatePresentationRequest;
import presentation.dtos.request.spareRequestPresentationDto.SparePartFilterPresentationRequest;
import presentation.dtos.request.spareRequestPresentationDto.SparePartStockUpdatePresentationRequest;
import presentation.dtos.response.spareResponsePresentationDto.SparePartPresentationResponse;
import presentation.dtos.response.spareResponsePresentationDto.SparePartListPresentationResponse;
import presentation.dtos.response.spareResponsePresentationDto.SparePartCompatibilityPresentationResponse;
import presentation.dtos.response.spareResponsePresentationDto.CompatibleModelPresentationDto;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SparePartPresentationMapper {

    public CreateSparePartRequest toApplication(SparePartCreatePresentationRequest request) {
        if (request == null) return null;

        CreateSparePartRequest target = new CreateSparePartRequest();
        target.setSpareType(request.getSpareType());
        target.setName(request.getName());
        target.setDescription(request.getDescription());
        target.setManufacturer(request.getManufacturer());
        target.setPartNumber(request.getPartNumber());
        target.setPrice(request.getPrice());
        target.setCurrency(request.getCurrency());
        target.setQuantity(request.getQuantity());
        target.setCompatibleModelIds(request.getCompatibleModelIds());
        target.setSectionId(request.getSectionId());
        target.setLocation(request.getLocation());

        return target;
    }

    public UpdateSparePartRequest toApplication(SparePartUpdatePresentationRequest request) {
        if (request == null) return null;

        UpdateSparePartRequest target = new UpdateSparePartRequest();
        target.setName(request.getName());
        target.setDescription(request.getDescription());
        target.setManufacturer(request.getManufacturer());
        target.setPartNumber(request.getPartNumber());
        target.setPrice(request.getPrice());
        target.setSpareType(request.getSpareType());
        target.setCompatibleModelIds(request.getCompatibleModelIds());
        target.setUpdateReason(request.getUpdateReason());

        return target;
    }

    public SparePartFilterRequest toApplication(SparePartFilterPresentationRequest request) {
        if (request == null) return new SparePartFilterRequest();

        SparePartFilterRequest target = new SparePartFilterRequest();
        target.setSpareType(request.getSpareType());
        target.setManufacturer(request.getManufacturer());
        target.setMinPrice(request.getMinPrice());
        target.setMaxPrice(request.getMaxPrice());
        target.setCompatibleModelId(request.getCompatibleModelId());
        target.setInStock(request.getInStock());
        target.setLowStock(request.getLowStock());
        target.setSearchQuery(request.getSearchQuery());

        target.setPage(request.getPage());
        target.setSize(request.getSize());
        target.setSortBy(request.getSortBy());
        target.setSortDirection(request.getSortDirection());

        return target;
    }

    public UpdateStockRequest toApplication(SparePartStockUpdatePresentationRequest request) {
        if (request == null) return null;

        UpdateStockRequest target = new UpdateStockRequest();
        target.setSparePartId(request.getSparePartId());
        target.setNewQuantity(request.getNewQuantity());
        target.setReason(request.getReason());
        target.setSectionId(request.getSectionId());
        target.setLocation(request.getLocation());

        return target;
    }

    public SparePartPresentationResponse toPresentation(SparePartResponse source) {
        if (source == null) return null;

        return SparePartPresentationResponse.builder()
                .id(source.getId())
                .spareType(source.getSpareType())
                .spareTypeDisplayName(source.getSpareTypeDisplayName())
                .name(source.getName())
                .description(source.getDescription())
                .manufacturer(source.getManufacturer())
                .partNumber(source.getPartNumber())
                .price(source.getPrice())
                .priceFormatted(source.getPriceFormatted())
                .quantity(source.getQuantity())
                .status(source.getStatus())
                .statusDisplayName(source.getStatusDisplayName())
                .compatibleModelsCount(source.getCompatibleModelsCount())
                .sectionId(source.getSectionId())
                .location(source.getLocation())
                .lastUpdated(source.getLastUpdated())
                .inStock(source.isInStock())
                .lowStock(source.isLowStock())
                .outOfStock(source.isOutOfStock())
                .build();
    }

    public SparePartListPresentationResponse toPresentation(SparePartListResponse source) {
        if (source == null) return null;

        return SparePartListPresentationResponse.builder()
                .spareParts(source.getSpareParts().stream()
                        .map(this::toPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.getTotalCount())
                .inStockCount(source.getInStockCount())
                .lowStockCount(source.getLowStockCount())
                .outOfStockCount(source.getOutOfStockCount())
                .countByType(source.getCountByType())
                .build();
    }

    public SparePartCompatibilityPresentationResponse toPresentation(SparePartCompatibilityResponse source) {
        if (source == null) return null;

        return SparePartCompatibilityPresentationResponse.builder()
                .sparePartId(source.getSparePartId())
                .sparePartName(source.getSparePartName())
                .compatibleModels(source.getCompatibleModels() == null ? null :
                        source.getCompatibleModels().stream()
                                .map(this::toPresentation)
                                .collect(Collectors.toList()))
                .build();
    }

    public CompatibleModelPresentationDto toPresentation(CompatibleModelDto source) {
        if (source == null) return null;

        return CompatibleModelPresentationDto.builder()
                .modelId(source.getModelId())
                .modelName(source.getModelName())
                .brandName(source.getBrandName())
                .compatible(source.isCompatible())
                .build();
    }

    public SparePartListPresentationResponse toListPresentation(List<SparePartResponse> source) {
        if (source == null || source.isEmpty()) {
            return SparePartListPresentationResponse.builder()
                    .spareParts(List.of())
                    .totalCount(0)
                    .inStockCount(0)
                    .lowStockCount(0)
                    .outOfStockCount(0)
                    .countByType(Map.of())
                    .build();
        }

        long inStockCount = source.stream().filter(SparePartResponse::isInStock).count();
        long lowStockCount = source.stream().filter(SparePartResponse::isLowStock).count();
        long outOfStockCount = source.stream().filter(SparePartResponse::isOutOfStock).count();

        Map<String, Integer> countByType = source.stream()
                .collect(Collectors.groupingBy(
                        SparePartResponse::getSpareType,
                        Collectors.summingInt(p -> 1)
                ));

        return SparePartListPresentationResponse.builder()
                .spareParts(source.stream()
                        .map(this::toPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.size())
                .inStockCount((int) inStockCount)
                .lowStockCount((int) lowStockCount)
                .outOfStockCount((int) outOfStockCount)
                .countByType(countByType)
                .build();
    }
}