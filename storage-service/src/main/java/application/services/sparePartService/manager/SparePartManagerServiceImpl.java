package application.services.sparePartService.manager;

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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SparePartManagerServiceImpl extends BaseSparePartService implements SparePartManagerService {

    public SparePartManagerServiceImpl(
            SparePartRepository sparePartRepository,
            CarRepository carRepository,
            SparePartMapper sparePartMapper) {
        super(sparePartRepository, carRepository, sparePartMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SparePartResponse> getLowStockParts(int threshold) {
        log.debug("Getting low stock parts with threshold: {}", threshold);

        List<SparePart> lowStockParts = sparePartRepository.findLowStock(threshold);
        return lowStockParts.stream()
                .map(sparePart -> {
                    int quantity = sparePartRepository.getStockQuantity(sparePart.getId());
                    return sparePartMapper.toResponse(sparePart, quantity, null, null);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SparePartResponse> getOutOfStockParts() {
        log.debug("Getting out of stock parts");

        List<SparePart> outOfStockParts = sparePartRepository.findOutOfStock();
        return outOfStockParts.stream()
                .map(sparePart -> {
                    int quantity = sparePartRepository.getStockQuantity(sparePart.getId());
                    return sparePartMapper.toResponse(sparePart, quantity, null, null);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void requestRestock(String sparePartId, int quantity) {
        String managerId = SecurityUtils.getCurrentUserId();
        log.info("Restock requested for spare part {} by manager {}, quantity: {}",
                sparePartId, managerId, quantity);

        SparePart sparePart = findSparePartById(sparePartId);
        int currentStock = sparePartRepository.getStockQuantity(sparePartId);

        if (quantity <= 0) {
            throw new DomainValidationException("Restock quantity must be positive");
        }

        log.info("Restock request recorded for spare part: {} (current stock: {}, requested: {})",
                sparePart.getName(), currentStock, quantity);
    }
}