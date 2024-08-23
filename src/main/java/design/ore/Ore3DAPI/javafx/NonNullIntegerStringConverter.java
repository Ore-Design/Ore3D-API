package design.ore.Ore3DAPI.javafx;

import javafx.util.StringConverter;

public class NonNullIntegerStringConverter extends StringConverter<Number>
{
    public NonNullIntegerStringConverter() {}

    @Override public Number fromString(String value)
    {
        if (value == null) return 0;

        value = value.trim();

        if (value.length() < 1) return 0;

        return Integer.valueOf(value);
    }

    @Override public String toString(Number value)
    {
        if (value == null) return "";
        
        return value.intValue() + "";
    }
}