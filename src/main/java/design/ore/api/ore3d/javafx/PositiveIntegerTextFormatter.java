package design.ore.api.ore3d.javafx;

import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.UnaryOperator;

public class PositiveIntegerTextFormatter extends TextFormatter<Integer>
{
    public PositiveIntegerTextFormatter() { super(new IntegerStringConverter(), 0, getUnaryOperator()); }

    private static UnaryOperator<javafx.scene.control.TextFormatter.Change> getUnaryOperator()
    {
        UnaryOperator<Change> integerFilter = change -> {
        	String newText = change.getControlNewText();
            if (newText.matches("\\d*")) return change;
            return null;
        };
        
        return integerFilter;
    }
}
