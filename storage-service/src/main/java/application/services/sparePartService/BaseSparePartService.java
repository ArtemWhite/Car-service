package application.services.sparePartService;

import application.mapper.SparePartMapper;
import domain.exception.EntityNotFoundException;
import domain.models.car.CarModel;
import domain.models.sparePart.SparePart;
import domain.repository.carRepository.CarRepository;
import domain.repository.sparePartRepository.SparePartRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
public abstract class BaseSparePartService {

    protected final SparePartRepository sparePartRepository;
    protected final CarRepository carRepository;
    protected final SparePartMapper sparePartMapper;

    public BaseSparePartService(
            SparePartRepository sparePartRepository,
            CarRepository carRepository,
            SparePartMapper sparePartMapper) {
        this.sparePartRepository = sparePartRepository;
        this.carRepository = carRepository;
        this.sparePartMapper = sparePartMapper;
    }

    protected SparePart findSparePartById(String id) {
        return sparePartRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Spare part not found: " + id));
    }

    protected Set<CarModel> findCompatibleModels(Set<String> modelIds) {
        log.debug("findCompatibleModels called with modelIds: {}", modelIds);
        Set<CarModel> models = new HashSet<>();
        if (modelIds != null) {
            for (String modelId : modelIds) {
                log.debug("Looking for modelId: {}", modelId);
                Optional<CarModel> modelOpt = carRepository.findModelById(modelId);
                modelOpt.ifPresent(models::add);
            }
        }
        log.debug("Total models found: {}", models.size());
        return models;
    }

    protected SparePart saveSparePart(SparePart sparePart) {
        return sparePartRepository.save(sparePart);
    }
}