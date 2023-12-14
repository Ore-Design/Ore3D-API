package design.ore.Ore3DAPI.JavaFX;

import javafx.util.StringConverter;

public class NonNullDoubleStringConverter extends StringConverter<Number>
{
    public NonNullDoubleStringConverter() {}

    @Override public Number fromString(String value)
    {
        if (value == null) return 0d;

        value = value.trim();

        if (value.length() < 1) return 0d;

        return Double.valueOf(value);
    }

    @Override public String toString(Number value)
    {
        if (value == null) return "";
        
        return value.toString();
    }
}