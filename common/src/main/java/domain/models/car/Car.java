package domain.models.car;

import domain.models.car.engine.Engine;
import domain.models.car.transmission.Transmission;
import domain.exception.DomainValidationException;
import domain.exception.IncompatibleComponentException;
import domain.models.car.types.*;
import lombok.Getter;

@Getter
public class Car
{
    private final String id;
    private final CarBrand brand;
    private final CarModel model;
    private final CarBody body;
    private final CarColor color;
    private final DriveType driveType;
    private final Transmission transmission;
    private final Engine engine;
    private CarConfiguration carConfiguration;
    private CarStatus carStatus;
    private Price price;

    public Car(String id, CarBrand brand, CarModel model, CarBody body, CarColor color, DriveType driveType,
               Engine engine, Transmission transmission, Price price)
    {
        if (model == null) {
            throw new DomainValidationException("Car model cannot be null");
        }

        if (brand == null) {
            throw new DomainValidationException("Car brand cannot be null");
        }

        if (!model.getCarBrand().equals(brand)) {
            throw new DomainValidationException("Model does not belong to selected brand");
        }

        this.id = id;
        this.brand = brand;
        this.model = model;
        this.body = body;
        this.color = color;
        this.driveType = driveType;
        this.engine = engine;
        this.transmission = transmission;
        this.price = price;
        this.carStatus = CarStatus.UNAVAILABLE;
    }

    public void applyConfiguration(CarConfiguration configuration)
    {
        if (configuration == null)
        {
            throw new DomainValidationException("Configuration cannot be null");
        }
        if (!configuration.isValidForModel(this.model))
        {
            throw new IncompatibleComponentException("Configuration is not for this car model");
        }
        this.carConfiguration = configuration;

        this.price = configuration.calculateTotalPrice(configuration.getBaseComponents());
    }

    public CarConfiguration getConfiguration()
    {
        return carConfiguration;
    }

    public void setPrice(Price price)
    {
        if (price == null)
        {
            throw new DomainValidationException("Price cannot be null");
        }
        this.price = price;
    }

    public void markAsAvailable() {
        if (carStatus == CarStatus.SOLD) {
            throw new DomainValidationException("Cannot mark sold car as available");
        }
        this.carStatus = CarStatus.AVAILABLE;
    }

    public void markAsSold() {
        if (this.carStatus != CarStatus.AVAILABLE &&
                this.carStatus != CarStatus.RESERVED) {
            throw new DomainValidationException("Car is not available for sale");
        }
        this.carStatus = CarStatus.SOLD;
    }

    public void addToTestDriveFleet() {
        if (carStatus != CarStatus.AVAILABLE && carStatus != CarStatus.IN_STOCK && carStatus != CarStatus.ON_TEST_DRIVE) {
            throw new DomainValidationException("Car cannot be used for test drives");
        }
        this.carStatus = CarStatus.TEST_DRIVE_AVAILABLE;
    }

    public void markAsTestDriveStarted() {
        if (carStatus != CarStatus.TEST_DRIVE_AVAILABLE) {
            throw new DomainValidationException("Car cannot be used for test drives");
        }
        this.carStatus = CarStatus.ON_TEST_DRIVE;
    }

    public void reserve() {
        if (carStatus != CarStatus.AVAILABLE) {
            throw new DomainValidationException("Car is not available for reservation");
        }
        this.carStatus = CarStatus.RESERVED;
    }

    public void markAsInService() {
        this.carStatus = CarStatus.IN_SERVICE;
    }

    public void markAsBooked() {
        if (carStatus != CarStatus.AVAILABLE) {
            throw new DomainValidationException("Car is not available for booking");
        }
        this.carStatus = CarStatus.BOOKED;
    }

    public boolean isAvailableForTestDrive() {
        return carStatus == CarStatus.TEST_DRIVE_AVAILABLE;
    }

    public boolean isAvailableForPurchase() {
        return carStatus == CarStatus.AVAILABLE ||
                carStatus == CarStatus.IN_STOCK;
    }

    public void markAsUnavailable()
    {
        this.carStatus = CarStatus.UNAVAILABLE;
    }

    public String getCarId() { return id; }

    public String getCarInfo() {
        if (model == null) {
            return String.format("%s, %s, %s, %s",
                    brand.getDisplayName(),
                    engine.getDescription(),
                    transmission.getFullName(),
                    color.getDisplayName()
            );
        }
        return String.format("%s %s, %s, %s, %s",
                brand.getDisplayName(),
                model.getName(),
                engine.getDescription(),
                transmission.getFullName(),
                color.getDisplayName()
        );
    }

    public boolean isSold() {
        return carStatus == CarStatus.SOLD;
    }

    public boolean isOnTestDrive() {
        return carStatus == CarStatus.ON_TEST_DRIVE;
    }

    public boolean isReserved() {
        return carStatus == CarStatus.RESERVED;
    }

    public boolean isUnavailable() {
        return carStatus == CarStatus.UNAVAILABLE;
    }

    public boolean isInService() {
        return carStatus == CarStatus.IN_SERVICE;
    }

    public void restoreStatus(CarStatus status) {
        this.carStatus = status;
    }
}
