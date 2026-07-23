package domainTest.sparePart.spareType;

import domain.models.sparePart.SpareType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

class SpareTypeTest {

    @Test
    @DisplayName("Should have all 31 spare types")
    void shouldHaveAllSpareTypes() {
        SpareType[] types = SpareType.values();

        assertEquals(30, types.length);
    }

    @Test
    @DisplayName("Should have correct display names for all types")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Масляный фильтр", SpareType.OIL_FILTER.getDisplayName());
        assertEquals("Воздушный фильтр", SpareType.AIR_FILTER.getDisplayName());
        assertEquals("Тормозные колодки", SpareType.BRAKE_PADS.getDisplayName());
        assertEquals("Тормозные диски", SpareType.BRAKE_DISCS.getDisplayName());
        assertEquals("Свечи зажигания", SpareType.SPARK_PLUG.getDisplayName());
        assertEquals("Аккумулятор", SpareType.BATTERY.getDisplayName());
        assertEquals("Ремень ГРМ", SpareType.TIMING_BELT.getDisplayName());
        assertEquals("Генератор", SpareType.ALTERNATOR.getDisplayName());
        assertEquals("Стартер", SpareType.STARTER.getDisplayName());
        assertEquals("Амортизатор", SpareType.SHOCK_ABSORBER.getDisplayName());
        assertEquals("Пружина", SpareType.SPRING.getDisplayName());
        assertEquals("Ступичный подшипник", SpareType.WHEEL_BEARING.getDisplayName());
        assertEquals("Шина", SpareType.TIRE.getDisplayName());
        assertEquals("Диск колесный", SpareType.WHEEL.getDisplayName());
        assertEquals("Фара", SpareType.HEADLIGHT.getDisplayName());
        assertEquals("Фонарь задний", SpareType.TAILLIGHT.getDisplayName());
        assertEquals("Бампер", SpareType.BUMPER.getDisplayName());
        assertEquals("Дверь", SpareType.DOOR.getDisplayName());
        assertEquals("Зеркало", SpareType.MIRROR.getDisplayName());
        assertEquals("Лобовое стекло", SpareType.WINDSHIELD.getDisplayName());
        assertEquals("Радиатор", SpareType.RADIATOR.getDisplayName());
        assertEquals("Помпа", SpareType.WATER_PUMP.getDisplayName());
        assertEquals("Топливный насос", SpareType.FUEL_PUMP.getDisplayName());
        assertEquals("Глушитель", SpareType.EXHAUST_PIPE.getDisplayName());
        assertEquals("Катализатор", SpareType.CATALYST.getDisplayName());
        assertEquals("Датчик", SpareType.SENSOR.getDisplayName());
        assertEquals("Предохранитель", SpareType.FUSE.getDisplayName());
        assertEquals("Щетки стеклоочистителя", SpareType.WIPER.getDisplayName());
        assertEquals("Моторное масло", SpareType.OIL.getDisplayName());
        assertEquals("Охлаждающая жидкость", SpareType.COOLANT.getDisplayName());
    }

    @Test
    @DisplayName("Should maintain correct ordinal values")
    void shouldMaintainCorrectOrdinal() {
        assertEquals(0, SpareType.OIL_FILTER.ordinal());
        assertEquals(1, SpareType.AIR_FILTER.ordinal());
        assertEquals(2, SpareType.BRAKE_PADS.ordinal());
        assertEquals(3, SpareType.BRAKE_DISCS.ordinal());
        assertEquals(4, SpareType.SPARK_PLUG.ordinal());
        assertEquals(5, SpareType.BATTERY.ordinal());
        assertEquals(6, SpareType.TIMING_BELT.ordinal());
        assertEquals(7, SpareType.ALTERNATOR.ordinal());
        assertEquals(8, SpareType.STARTER.ordinal());
        assertEquals(9, SpareType.SHOCK_ABSORBER.ordinal());
        assertEquals(10, SpareType.SPRING.ordinal());
        assertEquals(11, SpareType.WHEEL_BEARING.ordinal());
        assertEquals(12, SpareType.TIRE.ordinal());
        assertEquals(13, SpareType.WHEEL.ordinal());
        assertEquals(14, SpareType.HEADLIGHT.ordinal());
        assertEquals(15, SpareType.TAILLIGHT.ordinal());
        assertEquals(16, SpareType.BUMPER.ordinal());
        assertEquals(17, SpareType.DOOR.ordinal());
        assertEquals(18, SpareType.MIRROR.ordinal());
        assertEquals(19, SpareType.WINDSHIELD.ordinal());
        assertEquals(20, SpareType.RADIATOR.ordinal());
        assertEquals(21, SpareType.WATER_PUMP.ordinal());
        assertEquals(22, SpareType.FUEL_PUMP.ordinal());
        assertEquals(23, SpareType.EXHAUST_PIPE.ordinal());
        assertEquals(24, SpareType.CATALYST.ordinal());
        assertEquals(25, SpareType.SENSOR.ordinal());
        assertEquals(26, SpareType.FUSE.ordinal());
        assertEquals(27, SpareType.WIPER.ordinal());
        assertEquals(28, SpareType.OIL.ordinal());
        assertEquals(29, SpareType.COOLANT.ordinal());
    }

    @Test
    @DisplayName("Should convert from string correctly")
    void shouldConvertFromString() {
        assertEquals(SpareType.OIL_FILTER, SpareType.valueOf("OIL_FILTER"));
        assertEquals(SpareType.BRAKE_PADS, SpareType.valueOf("BRAKE_PADS"));
        assertEquals(SpareType.BATTERY, SpareType.valueOf("BATTERY"));
        assertEquals(SpareType.WHEEL, SpareType.valueOf("WHEEL"));
        assertEquals(SpareType.COOLANT, SpareType.valueOf("COOLANT"));
    }

    @Test
    @DisplayName("Should have unique display names")
    void shouldHaveUniqueDisplayNames() {
        SpareType[] types = SpareType.values();

        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                assertNotEquals(types[i].getDisplayName(), types[j].getDisplayName());
            }
        }
    }
}