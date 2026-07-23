package application.services.sparePartService.systemAdmin;

import application.dtos.request.spareRequest.CreateSparePartRequest;
import application.dtos.request.spareRequest.UpdateSparePartRequest;
import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import application.services.sparePartService.BaseSparePartService;
import domain.exception.EntityNotFoundException;
import domain.models.car.CarModel;
import domain.models.sparePart.SparePart;
import domain.repository.carRepository.CarRepository;
import domain.repository.sparePartRepository.SparePartRepository;
import infrastructure.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class SparePartSystemAdminServiceImpl extends BaseSparePartService implements SparePartSystemAdminService {

    public SparePartSystemAdminServiceImpl(
            SparePartRepository sparePartRepository,
            CarRepository carRepository,
            SparePartMapper sparePartMapper) {
        super(sparePartRepository, carRepository, sparePartMapper);
    }

    @Override
    public SparePartResponse createSparePart(CreateSparePartRequest request) {
        String adminId = SecurityUtils.getCurrentUserId();
        log.info("Creating spare part by admin: {}", adminId);

        Set<CarModel> compatibleModels = findCompatibleModels(request.getCompatibleModelIds());

        SparePart sparePart = sparePartMapper.toDomain(request, compatibleModels);
        SparePart saved = saveSparePart(sparePart);

        sparePartRepository.updateStock(
                saved.getId(),
                request.getQuantity() != null ? request.getQuantity() : 0,
                request.getSectionId(),
                request.getLocation()
        );

        int quantity = sparePartRepository.getStockQuantity(saved.getId());
        log.info("Spare part created with id: {}", saved.getId());

        return sparePartMapper.toResponse(saved, quantity, request.getSectionId(), request.getLocation());
    }

    @Override
    public SparePartResponse updateSparePart(String id, UpdateSparePartRequest request) {
        String adminId = SecurityUtils.getCurrentUserId();
        log.info("Updating spare part {} by admin: {}", id, adminId);

        SparePart sparePart = findSparePartById(id);

        Set<CarModel> newCompatibleModels = null;
        if (request.getCompatibleModelIds() != null && !request.getCompatibleModelIds().isEmpty()) {
            newCompatibleModels = findCompatibleModels(request.getCompatibleModelIds());
        }

        sparePartMapper.updateDomain(sparePart, request, newCompatibleModels);
        SparePart updated = saveSparePart(sparePart);

        int quantity = sparePartRepository.getStockQuantity(id);
        String sectionId = sparePartRepository.getSectionId(id);
        String location = sparePartRepository.getLocation(id);

        log.info("Spare part updated: {}", id);
        return sparePartMapper.toResponse(updated, quantity, sectionId, location);
    }

    @Override
    public void deleteSparePart(String id, String reason) {
        String adminId = SecurityUtils.getCurrentUserId();
        log.info("Deleting spare part {} by admin: {}, reason: {}", id, adminId, reason);

        findSparePartById(id);
        sparePartRepository.delete(id);
        log.info("Spare part deleted: {}", id);
    }

    @Override
    public void addCompatibleModel(String sparePartId, String modelId) {
        String adminId = SecurityUtils.getCurrentUserId();
        log.info("Adding compatible model {} to spare part {} by admin: {}", modelId, sparePartId, adminId);

        SparePart sparePart = findSparePartById(sparePartId);
        CarModel model = carRepository.findModelById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Car model not found: " + modelId));

        sparePart.addCompatibleModel(model);
        saveSparePart(sparePart);
        log.info("Compatible model added");
    }

    @Override
    public void removeCompatibleModel(String sparePartId, String modelId) {
        String adminId = SecurityUtils.getCurrentUserId();
        log.info("Removing compatible model {} from spare part {} by admin: {}", modelId, sparePartId, adminId);

        SparePart sparePart = findSparePartById(sparePartId);
        sparePart.removeCompatibleModel(modelId);
        saveSparePart(sparePart);
        log.info("Compatible model removed");
    }

    @Override
    @Transactional(readOnly = true)
    public SparePartResponse getSparePartById(String id) {
        SparePart sparePart = findSparePartById(id);
        int quantity = sparePartRepository.getStockQuantity(id);
        String sectionId = sparePartRepository.getSectionId(id);
        String location = sparePartRepository.getLocation(id);
        return sparePartMapper.toResponse(sparePart, quantity, sectionId, location);
    }
}