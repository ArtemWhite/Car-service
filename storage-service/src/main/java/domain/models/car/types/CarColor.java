package domain.models.car.types;

public enum CarColor
{
    RED("Красный",false, 20000),
    GREEN("Зелёный", false, 21000),
    WHITE("Белый",true, 0),
    BLACK("Чёрный", true, 0);

    private final String displayName;
    private final boolean defaultColor;
    private final int colorPrice;

    CarColor (String displayName, boolean defaultColor, int colorPrice)
    {
        this.displayName = displayName;
        this.defaultColor = defaultColor;
        this.colorPrice = colorPrice;
    }

    public String getDisplayName()
    {
        return displayName;
    }
    public boolean isDefaultColor()
    {
        return defaultColor;
    }

    public int getColorPrice()
    {
        return colorPrice;
    }
}
