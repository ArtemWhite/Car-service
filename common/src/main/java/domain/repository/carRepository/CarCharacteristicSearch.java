package domain.repository.carRepository;

import domain.models.car.Car;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.types.CarBrand;

import java.util.List;

public interface CarCharacteristicSearch
{
    List<Car> findByBrand(CarBrand brand);
    List<Car> findByModel(CarModel model);
    List<Car> findByPriceRange(Price minPrice, Price maxPrice);

    List<Car> findByDriveType(String driveType);
    List<Car> findByColor(String color);
    List<Car> findByBody(String body);
    List<Car> findByBrandAndModel(String brand, String model);
    List<Object[]> countCarsByBrand();
}