package application.services.sparePartService.warehouseAdmin;

import application.dtos.request.spareRequest.UpdateStockRequest;
import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import application.services.sparePartService.BaseSparePartService;
import domain.exception.DomainValidationException;
import domain.models.sparePart.SparePart;
import domain.repository.carRepository.CarRepository;
import domain.repository.sparePartRepository.SparePartRepository;
import infrastructure.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class SparePartWarehouseAdminServiceImpl extends BaseSparePartService implements SparePartWarehouseAdminService {

    public SparePartWarehouseAdminServiceImpl(
            SparePartRepository sparePartRepository,
            CarRepository carRepository,
            SparePartMapper sparePartMapper) {
        super(sparePartRepository, carRepository, sparePartMapper);
    }

    @Override
    public SparePartResponse updateStock(UpdateStockRequest request) {
        String adminId = SecurityUtils.getCurrentUserId();
        log.info("Updating stock for spare part {} by admin: {}", request.getSparePartId(), adminId);

        SparePart sparePart = findSparePartById(request.getSparePartId());
        int oldQuantity = sparePartRepository.getStockQuantity(request.getSparePartId());

        sparePartRepository.updateStock(
                request.getSparePartId(),
                request.getNewQuantity(),
                request.getSectionId(),
                request.getLocation()
        );

        log.info("Stock updated: {} -> {} for spare part {}", oldQuantity, request.getNewQuantity(), sparePart.getName());

        return sparePartMapper.toResponse(
                sparePart,
                request.getNewQuantity(),
                request.getSectionId(),
                request.getLocation()
        );
    }

    @Override
    public SparePartResponse receiveShipment(String sparePartId, int quantity) {
        String adminId = SecurityUtils.getCurrentUserId();
        log.info("Receiving shipment of {} units for spare part {} by admin: {}", quantity, sparePartId, adminId);

        SparePart sparePart = findSparePartById(sparePartId);

        if (quantity <= 0) {
            throw new DomainValidationException("Shipment quantity must be positive");
        }

        int currentQuantity = sparePartRepository.getStockQuantity(sparePartId);
        int newQuantity = currentQuantity + quantity;

        sparePartRepository.updateStock(sparePartId, newQuantity, null, null);
        log.info("Shipment received. New quantity: {}", newQuantity);

        return sparePartMapper.toResponse(sparePart, newQuantity, null, null);
    }

    @Override
    public SparePartResponse moveToLocation(String sparePartId, String section, String location) {
        String adminId = SecurityUtils.getCurrentUserId();
        log.info("Moving spare part {} to {}/{} by admin: {}", sparePartId, section, location, adminId);

        SparePart sparePart = findSparePartById(sparePartId);
        int currentQuantity = sparePartRepository.getStockQuantity(sparePartId);

        sparePartRepository.updateStock(sparePartId, currentQuantity, section, location);
        log.info("Spare part moved to new location");

        return sparePartMapper.toResponse(sparePart, currentQuantity, section, location);
    }

    @Override
    public SparePartResponse writeOff(String sparePartId, int quantity, String reason) {
        String adminId = SecurityUtils.getCurrentUserId();
        log.info("Writing off {} units of spare part {} by admin: {}, reason: {}", quantity, sparePartId, adminId, reason);

        SparePart sparePart = findSparePartById(sparePartId);

        if (quantity <= 0) {
            throw new DomainValidationException("Write-off quantity must be positive");
        }

        int currentQuantity = sparePartRepository.getStockQuantity(sparePartId);

        if (currentQuantity < quantity) {
            throw new DomainValidationException(
                    "Cannot write off " + quantity + " units. Only " + currentQuantity + " in stock"
            );
        }

        int newQuantity = currentQuantity - quantity;
        sparePartRepository.updateStock(sparePartId, newQuantity, null, null);
        log.info("Write-off completed. New quantity: {}", newQuantity);

        return sparePartMapper.toResponse(sparePart, newQuantity, null, null);
    }
}