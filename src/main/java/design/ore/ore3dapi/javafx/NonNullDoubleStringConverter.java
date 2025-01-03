package design.ore.ore3dapi.javafx;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        
        return new BigDecimal(value.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue() + "";
    }
}