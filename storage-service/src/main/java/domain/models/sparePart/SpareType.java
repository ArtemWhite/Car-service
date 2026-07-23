package domain.models.sparePart;

import lombok.Getter;

@Getter
public enum SpareType
{
    OIL_FILTER("Масляный фильтр"),
    AIR_FILTER("Воздушный фильтр"),
    BRAKE_PADS("Тормозные колодки"),
    BRAKE_DISCS("Тормозные диски"),
    SPARK_PLUG("Свечи зажигания"),
    BATTERY("Аккумулятор"),
    TIMING_BELT("Ремень ГРМ"),
    ALTERNATOR("Генератор"),
    STARTER("Стартер"),
    SHOCK_ABSORBER("Амортизатор"),
    SPRING("Пружина"),
    WHEEL_BEARING("Ступичный подшипник"),
    TIRE("Шина"),
    WHEEL("Диск колесный"),
    HEADLIGHT("Фара"),
    TAILLIGHT("Фонарь задний"),
    BUMPER("Бампер"),
    DOOR("Дверь"),
    MIRROR("Зеркало"),
    WINDSHIELD("Лобовое стекло"),
    RADIATOR("Радиатор"),
    WATER_PUMP("Помпа"),
    FUEL_PUMP("Топливный насос"),
    EXHAUST_PIPE("Глушитель"),
    CATALYST("Катализатор"),
    SENSOR("Датчик"),
    FUSE("Предохранитель"),
    WIPER("Щетки стеклоочистителя"),
    OIL("Моторное масло"),
    COOLANT("Охлаждающая жидкость");

    private final String displayName;

    SpareType(String displayName) {
        this.displayName = displayName;
    }

}
