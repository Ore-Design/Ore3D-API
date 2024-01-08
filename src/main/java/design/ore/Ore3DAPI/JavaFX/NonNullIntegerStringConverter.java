package design.ore.Ore3DAPI.JavaFX;

import javafx.util.StringConverter;

public class NonNullIntegerStringConverter extends StringConverter<Integer>
{
    public NonNullIntegerStringConverter() {}

    @Override public Integer fromString(String value)
    {
        if (value == null) return 0;

        value = value.trim();

        if (value.length() < 1) return 0;

        return Integer.valueOf(value);
    }

    @Override public String toString(Integer value)
    {
        if (value == null) return "";
        
        return value.toString();
    }
}