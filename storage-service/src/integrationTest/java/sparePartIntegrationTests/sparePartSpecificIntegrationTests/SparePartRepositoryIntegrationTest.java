package sparePartIntegrationTests.sparePartSpecificIntegrationTests;

import domain.models.car.Price;
import org.junit.jupiter.api.DisplayName;
import sparePartIntegrationTests.SparePartBaseIntegrationTest;
import domain.models.sparePart.SpareType;
import domain.repository.sparePartRepository.SparePartRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class SparePartRepositoryIntegrationTest extends SparePartBaseIntegrationTest {

    @Autowired
    private SparePartRepository sparePartRepository;

    @Test
    void shouldSaveSparePart() throws Exception {
        String sparePartId = createSparePart("Repository Save Test", "OIL_FILTER", 1000.0, 10);

        var found = sparePartRepository.findById(sparePartId);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Repository Save Test");
    }

    @Test
    void shouldFindById() throws Exception {
        String sparePartId = createSparePart("Find By ID Test", "OIL_FILTER", 1000.0, 10);

        var found = sparePartRepository.findById(sparePartId);
        assertThat(found).isPresent();
    }

    @Test
    void shouldFindAll() throws Exception {
        createSparePart("Find All Test 1", "OIL_FILTER", 1000.0, 10);
        createSparePart("Find All Test 2", "BRAKE_PADS", 2000.0, 5);

        var all = sparePartRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldDeleteSparePart() throws Exception {
        String sparePartId = createSparePart("Delete Test", "OIL_FILTER", 1000.0, 10);

        sparePartRepository.delete(sparePartId);

        var found = sparePartRepository.findById(sparePartId);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindByType() throws Exception {
        createSparePart("Type Test 1", "OIL_FILTER", 1000.0, 10);
        createSparePart("Type Test 2", "OIL_FILTER", 1500.0, 5);
        createSparePart("Type Test 3", "BRAKE_PADS", 2000.0, 5);

        var oilFilters = sparePartRepository.findByType(SpareType.OIL_FILTER);
        assertThat(oilFilters.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldFindLowStock() throws Exception {
        createSparePart("Low Stock Repo Test", "OIL_FILTER", 1000.0, 3);

        var lowStock = sparePartRepository.findLowStock(5);
        assertThat(lowStock).isNotEmpty();
    }

    @Test
    void shouldFindOutOfStock() throws Exception {
        createSparePart("Out Stock Repo Test", "OIL_FILTER", 1000.0, 0);

        var outOfStock = sparePartRepository.findOutOfStock();
        assertThat(outOfStock).isNotEmpty();
    }

    @Test
    @DisplayName("Should count spare parts by type")
    void shouldCountByType() throws Exception {
        createSparePart("Count Type 1", "OIL_FILTER", 1000.0, 10);
        createSparePart("Count Type 2", "OIL_FILTER", 1500.0, 5);
        createSparePart("Count Type 3", "BRAKE_PADS", 2000.0, 5);

        long oilFilterCount = sparePartRepository.countByType(SpareType.OIL_FILTER);
        long brakePadsCount = sparePartRepository.countByType(SpareType.BRAKE_PADS);

        assertThat(oilFilterCount).isEqualTo(2);
        assertThat(brakePadsCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find by type and minimum stock")
    void shouldFindByTypeAndMinStock() throws Exception {
        createSparePart("Type Stock 1", "OIL_FILTER", 1000.0, 10);
        createSparePart("Type Stock 2", "OIL_FILTER", 1500.0, 3);
        createSparePart("Type Stock 3", "OIL_FILTER", 2000.0, 15);

        var parts = sparePartRepository.findByTypeAndStock(SpareType.OIL_FILTER, 5);
        assertThat(parts).hasSize(2);
    }

    @Test
    @DisplayName("Should find by price range")
    void shouldFindByPriceRange() throws Exception {
        createSparePart("Price 500", "OIL_FILTER", 500.0, 10);
        createSparePart("Price 1000", "OIL_FILTER", 1000.0, 10);
        createSparePart("Price 1500", "OIL_FILTER", 1500.0, 10);
        createSparePart("Price 2000", "OIL_FILTER", 2000.0, 10);

        Price minPrice = Price.of(800.0, "RUB");
        Price maxPrice = Price.of(1800.0, "RUB");

        var parts = sparePartRepository.findByPriceRange(minPrice, maxPrice);
        assertThat(parts).hasSize(2);
    }
}