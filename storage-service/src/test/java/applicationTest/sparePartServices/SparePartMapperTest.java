package applicationTest.sparePartServices;

import application.dtos.request.spareRequest.CreateSparePartRequest;
import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.types.CarBrand;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SparePartMapper Tests")
class SparePartMapperTest {

    private SparePartMapper sparePartMapper;
    private SparePart sparePart;
    private CarModel bmwModel;
    private CarModel audiModel;
    private Set<CarModel> compatibleModels;

    @BeforeEach
    void setUp() {
        sparePartMapper = new SparePartMapper();

        bmwModel = new CarModel("model1", "320i", CarBrand.BMW, "G20");
        audiModel = new CarModel("model2", "A4", CarBrand.AUDI, "B9");
        compatibleModels = Set.of(bmwModel, audiModel);

        sparePart = new SparePart(
                "part123",
                SpareType.BRAKE_PADS,
                "Brake Pads Premium",
                "High-quality brake pads",
                Price.of(5000, "RUB"),
                compatibleModels
        );
    }

    @Test
    @DisplayName("Should convert CreateSparePartRequest to SparePart")
    void shouldConvertCreateRequestToDomain() {
        CreateSparePartRequest request = new CreateSparePartRequest();
        request.setSpareType("BRAKE_PADS");
        request.setName("Brake Pads Premium");
        request.setDescription("High-quality brake pads");
        request.setPrice(5000.0);
        request.setCurrency("RUB");

        SparePart result = sparePartMapper.toDomain(request, compatibleModels);

        assertNotNull(result);
        assertEquals(SpareType.BRAKE_PADS, result.getType());
        assertEquals("Brake Pads Premium", result.getName());
        assertEquals("High-quality brake pads", result.getDescription());
        assertEquals(5000, result.getPrice().getAmount().doubleValue());
        assertEquals("RUB", result.getPrice().getCurrency().getCurrencyCode());
        assertEquals(2, result.getCompatibles().size());
        assertTrue(result.isCompatibleWith(bmwModel));
        assertTrue(result.isCompatibleWith(audiModel));
    }

    @Test
    @DisplayName("Should handle null description in toDomain")
    void shouldHandleNullDescriptionInToDomain() {
        CreateSparePartRequest request = new CreateSparePartRequest();
        request.setSpareType("BRAKE_PADS");
        request.setName("Brake Pads");
        request.setPrice(5000.0);

        SparePart result = sparePartMapper.toDomain(request, Set.of());

        assertNotNull(result);
        assertEquals("", result.getDescription());
    }

    @Test
    @DisplayName("Should convert SparePart to SparePartResponse")
    void shouldConvertDomainToResponse() {
        int quantity = 15;
        String sectionId = "A-01";
        String location = "shelf-3";

        SparePartResponse response = sparePartMapper.toResponse(sparePart, quantity, sectionId, location);

        assertNotNull(response);
        assertEquals("part123", response.getId());
        assertEquals("BRAKE_PADS", response.getSpareType());
        assertEquals("Тормозные колодки", response.getSpareTypeDisplayName());
        assertEquals("Brake Pads Premium", response.getName());
        assertEquals("High-quality brake pads", response.getDescription());
        assertEquals(5000, response.getPrice());
        assertNotNull(response.getPriceFormatted());
        assertEquals(15, response.getQuantity());
        assertEquals("A-01", response.getSectionId());
        assertEquals("shelf-3", response.getLocation());
        assertEquals(2, response.getCompatibleModelsCount());
        assertNotNull(response.getLastUpdated());
    }

    @Test
    @DisplayName("Should set inStock flag correctly for quantity > 5")
    void shouldSetInStockFlagForQuantityGreaterThan5() {
        SparePartResponse response = sparePartMapper.toResponse(sparePart, 10, null, null);

        assertTrue(response.isInStock());
        assertFalse(response.isLowStock());
        assertFalse(response.isOutOfStock());
        assertEquals("IN_STOCK", response.getStatus());
        assertEquals("В наличии", response.getStatusDisplayName());
    }

    @Test
    @DisplayName("Should set lowStock flag for quantity between 1 and 5")
    void shouldSetLowStockFlagForQuantityBetween1And5() {
        SparePartResponse response = sparePartMapper.toResponse(sparePart, 3, null, null);

        assertTrue(response.isInStock());
        assertTrue(response.isLowStock());
        assertFalse(response.isOutOfStock());
        assertEquals("LOW_STOCK", response.getStatus());
        assertEquals("Мало (менее 5 шт.)", response.getStatusDisplayName());
    }

    @Test
    @DisplayName("Should set outOfStock flag for quantity 0")
    void shouldSetOutOfStockFlagForQuantityZero() {
        SparePartResponse response = sparePartMapper.toResponse(sparePart, 0, null, null);

        assertFalse(response.isInStock());
        assertFalse(response.isLowStock());
        assertTrue(response.isOutOfStock());
        assertEquals("OUT_OF_STOCK", response.getStatus());
        assertEquals("Нет в наличии", response.getStatusDisplayName());
    }

    @Test
    @DisplayName("Should set outOfStock flag for negative quantity")
    void shouldSetOutOfStockFlagForNegativeQuantity() {
        SparePartResponse response = sparePartMapper.toResponse(sparePart, -5, null, null);

        assertFalse(response.isInStock());
        assertFalse(response.isLowStock());
        assertTrue(response.isOutOfStock());
    }

    @Test
    @DisplayName("Should format price correctly")
    void shouldFormatPriceCorrectly() {
        SparePartResponse response = sparePartMapper.toResponse(sparePart, 10, null, null);

        assertNotNull(response.getPriceFormatted());
        String formatted = response.getPriceFormatted().replace("\u00A0", " ");
        assertTrue(formatted.contains("5 000") || formatted.contains("5000"));
        assertTrue(formatted.contains("₽"));
    }

    @Test
    @DisplayName("Should convert list of SpareParts to list of Responses")
    void shouldConvertListOfDomainsToResponses() {
        List<SparePart> parts = List.of(sparePart, sparePart);

        List<SparePartResponse> responses = sparePartMapper.toResponseList(parts);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    @DisplayName("Should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
        List<SparePart> parts = List.of();

        List<SparePartResponse> responses = sparePartMapper.toResponseList(parts);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should convert list with stock maps to responses")
    void shouldConvertListWithStockMapsToResponses() {
        List<SparePart> parts = List.of(sparePart);
        java.util.Map<String, Integer> stockMap = java.util.Map.of("part123", 20);
        java.util.Map<String, String> sectionMap = java.util.Map.of("part123", "B-02");
        java.util.Map<String, String> locationMap = java.util.Map.of("part123", "shelf-5");

        List<SparePartResponse> responses = sparePartMapper.toResponseList(parts, stockMap, sectionMap, locationMap);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(20, responses.get(0).getQuantity());
        assertEquals("B-02", responses.get(0).getSectionId());
        assertEquals("shelf-5", responses.get(0).getLocation());
    }

    @Test
    @DisplayName("Should convert SparePart to Response with default values")
    void shouldConvertToResponseWithDefaults() {
        SparePartResponse response = sparePartMapper.toResponse(sparePart);

        assertNotNull(response);
        assertEquals(0, response.getQuantity());
        assertNull(response.getSectionId());
        assertNull(response.getLocation());
        assertTrue(response.isOutOfStock());
    }

    @Test
    @DisplayName("Should format price with ruble symbol")
    void shouldFormatPriceWithRubleSymbol() {
        SparePartResponse response = sparePartMapper.toResponse(sparePart, 10, null, null);

        assertTrue(response.getPriceFormatted().contains("₽"));
    }

    @Test
    @DisplayName("Should extract manufacturer from name containing bosch")
    void shouldExtractManufacturerFromBosch() {
        SparePart boschPart = new SparePart(
                "part456", SpareType.OIL_FILTER, "Bosch Oil Filter", "Premium filter",
                Price.of(1000, "RUB"), Set.of()
        );
        SparePartResponse response = sparePartMapper.toResponse(boschPart, 5, null, null);

        assertEquals("Bosch", response.getManufacturer());
    }

    @Test
    @DisplayName("Should extract manufacturer from name containing mann")
    void shouldExtractManufacturerFromMann() {
        SparePart mannPart = new SparePart(
                "part456", SpareType.AIR_FILTER, "Mann Air Filter", "Premium filter",
                Price.of(800, "RUB"), Set.of()
        );
        SparePartResponse response = sparePartMapper.toResponse(mannPart, 5, null, null);

        assertEquals("Mann-Filter", response.getManufacturer());
    }

    @Test
    @DisplayName("Should return unknown manufacturer for unknown name")
    void shouldReturnUnknownManufacturer() {
        SparePartResponse response = sparePartMapper.toResponse(sparePart, 5, null, null);

        assertEquals("Неизвестно", response.getManufacturer());
    }

    @Test
    @DisplayName("Should extract part number from id")
    void shouldExtractPartNumberFromId() {
        SparePartResponse response = sparePartMapper.toResponse(sparePart, 5, null, null);

        assertNotNull(response.getPartNumber());
        assertTrue(response.getPartNumber().startsWith("PN-"));
    }
}