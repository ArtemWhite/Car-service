package presentation.mappers;

import application.dtos.request.carRequest.CreateCarRequest;
import application.dtos.request.carRequest.UpdateCarRequest;
import application.dtos.request.carRequest.CarFilterRequest;
import application.dtos.request.carRequest.ApplyConfigurationRequest;
import application.dtos.response.carResponse.CarResponse;
import application.dtos.response.carResponse.CarListResponse;
import application.dtos.response.carResponse.componentResponse.CarConfigurationResponse;
import application.dtos.response.carResponse.componentResponse.ComponentResponse;

import presentation.dtos.request.carRequestPresentationDto.*;
import presentation.dtos.response.carResponsePresentationDto.*;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CarPresentationMapper {

    public CreateCarRequest toApplication(CarCreatePresentationRequest request) {
        if (request == null) return null;

        CreateCarRequest target = new CreateCarRequest();
        target.setBrand(request.getBrand());
        target.setModel(request.getModel());
        target.setBodyType(request.getBodyType());
        target.setColor(request.getColor());
        target.setDriveType(request.getDriveType());
        target.setEngineFuelType(request.getEngineFuelType());
        target.setEnginePower(request.getEnginePower());
        target.setEngineDisplacement(request.getEngineDisplacement());
        target.setTransmissionGears(request.getTransmissionGears());
        target.setTransmissionType(request.getTransmissionType());
        target.setPrice(request.getPrice());
        target.setConfigurationId(request.getConfigurationId());

        return target;
    }

    public UpdateCarRequest toApplication(CarUpdatePresentationRequest request) {
        if (request == null) return null;

        UpdateCarRequest target = new UpdateCarRequest();
        target.setPrice(request.getPrice());
        target.setStatus(request.getStatus());
        target.setConfigurationId(request.getConfigurationId());
        target.setUpdateReason(request.getUpdateReason());

        return target;
    }

    public CarFilterRequest toApplication(CarFilterPresentationRequest request) {
        if (request == null) return new CarFilterRequest();

        CarFilterRequest target = new CarFilterRequest();
        target.setMinPrice(request.getMinPrice());
        target.setMaxPrice(request.getMaxPrice());
        target.setBrand(request.getBrand());
        target.setModel(request.getModel());
        target.setBodyType(request.getBodyType());
        target.setFuelType(request.getFuelType());
        target.setMinPower(request.getMinPower());
        target.setMaxPower(request.getMaxPower());
        target.setMinEngineVolume(request.getMinEngineVolume());
        target.setMaxEngineVolume(request.getMaxEngineVolume());
        target.setTransmissionType(request.getTransmissionType());
        target.setDriveType(request.getDriveType());
        target.setColor(request.getColor());

        return target;
    }

    public ApplyConfigurationRequest toApplication(CarApplyConfigurationPresentationRequest request) {
        if (request == null) return null;

        ApplyConfigurationRequest target = new ApplyConfigurationRequest();
        target.setCarId(request.getCarId());
        target.setConfigurationId(request.getConfigurationId());
        target.setSelectedComponents(request.getSelectedComponents());

        return target;
    }

    public CarPresentationResponse toPresentation(CarResponse source) {
        if (source == null) return null;

        return CarPresentationResponse.builder()
                .id(source.getId())
                .brand(source.getBrand())
                .brandDisplayName(source.getBrandDisplayName())
                .brandCountry(source.getBrandCountry())
                .model(source.getModel())
                .modelFullName(source.getModelFullName())
                .bodyType(source.getBodyType())
                .bodyDisplayName(source.getBodyDisplayName())
                .color(source.getColor())
                .colorDisplayName(source.getColorDisplayName())
                .colorPrice(source.getColorPrice())
                .isDefaultColor(source.isDefaultColor())
                .driveType(source.getDriveType())
                .driveDisplayName(source.getDriveDisplayName())
                .driveCode(source.getDriveCode())
                .engineFuelType(source.getEngineFuelType())
                .engineFuelDisplayName(source.getEngineFuelDisplayName())
                .enginePower(source.getEnginePower())
                .engineDisplacement(source.getEngineDisplacement())
                .engineDescription(source.getEngineDescription())
                .transmissionType(source.getTransmissionType())
                .transmissionDisplayName(source.getTransmissionDisplayName())
                .transmissionGears(source.getTransmissionGears())
                .transmissionDescription(source.getTransmissionDescription())
                .price(source.getPrice())
                .priceFormatted(source.getPriceFormatted())
                .currency(source.getCurrency())
                .status(source.getStatus())
                .statusDisplayName(source.getStatusDisplayName())
                .configurationId(source.getConfigurationId())
                .configurationName(source.getConfigurationName())
                .availableForPurchase(source.isAvailableForPurchase())
                .availableForTestDrive(source.isAvailableForTestDrive())
                .inStock(source.isInStock())
                .reserved(source.isReserved())
                .sold(source.isSold())
                .carInfo(source.getCarInfo())
                .build();
    }

    public CarConfigurationPresentationResponse toPresentation(CarConfigurationResponse source) {
        if (source == null) return null;

        return CarConfigurationPresentationResponse.builder()
                .id(source.getId())
                .name(source.getName())
                .modelName(source.getModelName())
                .basePrice(source.getBasePrice())
                .basePriceValue(source.getBasePriceValue())
                .totalPrice(source.getTotalPrice())
                .totalPriceValue(source.getTotalPriceValue())
                .baseComponents(source.getBaseComponents() == null ? null :
                        source.getBaseComponents().stream()
                                .map(this::toPresentation)
                                .collect(Collectors.toList()))
                .availableComponents(source.getAvailableComponents() == null ? null :
                        source.getAvailableComponents().stream()
                                .map(this::toPresentation)
                                .collect(Collectors.toList()))
                .build();
    }

    public CarComponentPresentationResponse toPresentation(ComponentResponse source) {
        if (source == null) return null;

        return CarComponentPresentationResponse.builder()
                .id(source.getId())
                .type(source.getType())
                .typeDisplayName(source.getTypeDisplayName())
                .name(source.getName())
                .description(source.getDescription())
                .price(source.getPrice())
                .priceValue(source.getPriceValue())
                .selected(source.isSelected())
                .compatible(source.isCompatible())
                .build();
    }

    public CarListPresentationResponse toPresentation(CarListResponse source) {
        if (source == null) return null;

        return CarListPresentationResponse.builder()
                .cars(source.getCars().stream()
                        .map(this::toPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.getTotalCount())
                .availableCount(source.getAvailableCount())
                .testDriveCount(source.getTestDriveCount())
                .inStockCount(source.getInStockCount())
                .reservedCount(null)
                .soldCount(null)
                .minPrice(null)
                .maxPrice(null)
                .avgPrice(null)
                .build();
    }

    public CarListPresentationResponse toPresentation(List<CarResponse> source) {
        if (source == null || source.isEmpty()) {
            return CarListPresentationResponse.builder()
                    .cars(List.of())
                    .totalCount(0)
                    .availableCount(0)
                    .testDriveCount(0)
                    .inStockCount(0)
                    .reservedCount(0)
                    .soldCount(0)
                    .minPrice(null)
                    .maxPrice(null)
                    .avgPrice(null)
                    .build();
        }

        long availableCount = source.stream().filter(CarResponse::isAvailableForPurchase).count();
        long testDriveCount = source.stream().filter(CarResponse::isAvailableForTestDrive).count();
        long inStockCount = source.stream().filter(CarResponse::isInStock).count();
        long reservedCount = source.stream().filter(CarResponse::isReserved).count();
        long soldCount = source.stream().filter(CarResponse::isSold).count();

        Double minPrice = source.stream().mapToDouble(CarResponse::getPrice).min().orElse(0.0);
        Double maxPrice = source.stream().mapToDouble(CarResponse::getPrice).max().orElse(0.0);
        Double avgPrice = source.stream().mapToDouble(CarResponse::getPrice).average().orElse(0.0);

        return CarListPresentationResponse.builder()
                .cars(source.stream().map(this::toPresentation).collect(Collectors.toList()))
                .totalCount(source.size())
                .availableCount((int) availableCount)
                .testDriveCount((int) testDriveCount)
                .inStockCount((int) inStockCount)
                .reservedCount((int) reservedCount)
                .soldCount((int) soldCount)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .avgPrice(avgPrice)
                .build();
    }

    public CarListConfigurationPresentationResponse toConfigurationListPresentation(List<CarConfigurationResponse> source) {
        if (source == null || source.isEmpty()) {
            return CarListConfigurationPresentationResponse.builder()
                    .configurations(List.of())
                    .totalCount(0)
                    .build();
        }

        return CarListConfigurationPresentationResponse.builder()
                .configurations(source.stream()
                        .map(this::toPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.size())
                .build();
    }
}