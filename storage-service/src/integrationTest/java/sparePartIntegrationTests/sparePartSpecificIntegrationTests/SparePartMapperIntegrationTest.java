package sparePartIntegrationTests.sparePartSpecificIntegrationTests;

import sparePartIntegrationTests.SparePartBaseIntegrationTest;
import application.dtos.request.spareRequest.CreateSparePartRequest;
import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import domain.models.car.CarModel;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SparePartMapperIntegrationTest extends SparePartBaseIntegrationTest {

    @Autowired
    private SparePartMapper sparePartMapper;

    @Test
    void shouldMapCreateRequestToDomain() {
        CreateSparePartRequest request = new CreateSparePartRequest();
        request.setSpareType("OIL_FILTER");
        request.setName("Test Part");
        request.setDescription("Test Description");
        request.setPrice(1000.0);
        request.setCurrency("RUB");

        Set<CarModel> compatibleModels = Set.of();

        SparePart result = sparePartMapper.toDomain(request, compatibleModels);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(SpareType.OIL_FILTER);
        assertThat(result.getName()).isEqualTo("Test Part");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getPrice().getAmount().doubleValue()).isEqualTo(1000.0);
    }

    @Test
    void shouldMapDomainToResponse() throws Exception {
        String sparePartId = createSparePart("Test Mapper Part", "OIL_FILTER", 1500.0, 10);

        SparePart sparePart = new SparePart(
                sparePartId,
                SpareType.OIL_FILTER,
                "Test Mapper Part",
                "Description",
                domain.models.car.Price.of(1500.0, "RUB"),
                Set.of()
        );

        SparePartResponse response = sparePartMapper.toResponse(sparePart, 10, "SEC-01", "A-01");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(sparePartId);
        assertThat(response.getName()).isEqualTo("Test Mapper Part");
        assertThat(response.getQuantity()).isEqualTo(10);
        assertThat(response.getSectionId()).isEqualTo("SEC-01");
        assertThat(response.getLocation()).isEqualTo("A-01");
    }

    @Test
    void shouldCalculateInStockStatus() throws Exception {
        String sparePartId = createSparePart("In Stock Part", "OIL_FILTER", 1000.0, 10);

        SparePart sparePart = new SparePart(
                sparePartId,
                SpareType.OIL_FILTER,
                "In Stock Part",
                "Desc",
                domain.models.car.Price.of(1000.0, "RUB"),
                Set.of()
        );

        SparePartResponse response = sparePartMapper.toResponse(sparePart, 10, null, null);

        assertThat(response.isInStock()).isTrue();
        assertThat(response.isLowStock()).isFalse();
        assertThat(response.isOutOfStock()).isFalse();
        assertThat(response.getStatus()).isEqualTo("IN_STOCK");
    }

    @Test
    void shouldCalculateLowStockStatus() throws Exception {
        String sparePartId = createSparePart("Low Stock Part", "OIL_FILTER", 1000.0, 3);

        SparePart sparePart = new SparePart(
                sparePartId,
                SpareType.OIL_FILTER,
                "Low Stock Part",
                "Desc",
                domain.models.car.Price.of(1000.0, "RUB"),
                Set.of()
        );

        SparePartResponse response = sparePartMapper.toResponse(sparePart, 3, null, null);

        assertThat(response.isInStock()).isTrue();
        assertThat(response.isLowStock()).isTrue();
        assertThat(response.isOutOfStock()).isFalse();
        assertThat(response.getStatus()).isEqualTo("LOW_STOCK");
    }

    @Test
    void shouldCalculateOutOfStockStatus() throws Exception {
        String sparePartId = createSparePart("Out Stock Part", "OIL_FILTER", 1000.0, 0);

        SparePart sparePart = new SparePart(
                sparePartId,
                SpareType.OIL_FILTER,
                "Out Stock Part",
                "Desc",
                domain.models.car.Price.of(1000.0, "RUB"),
                Set.of()
        );

        SparePartResponse response = sparePartMapper.toResponse(sparePart, 0, null, null);

        assertThat(response.isInStock()).isFalse();
        assertThat(response.isLowStock()).isFalse();
        assertThat(response.isOutOfStock()).isTrue();
        assertThat(response.getStatus()).isEqualTo("OUT_OF_STOCK");
    }
}