package domain.repository.carRepository;

import domain.models.car.Car;
import domain.repository.BaseRepository;


public interface CarRepository extends
        BaseRepository<Car>,
        CarCharacteristicSearch,
        CarStatusSearch,
        CarFilterSearch,
        CarEngineSearch{
}