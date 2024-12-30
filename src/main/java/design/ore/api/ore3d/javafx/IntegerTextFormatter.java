package design.ore.api.ore3d.javafx;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

public class IntegerTextFormatter extends TextFormatter<Integer>
{
    public IntegerTextFormatter() { super(new IntegerStringConverter(), 0, getUnaryOperator()); }

    private static UnaryOperator<javafx.scene.control.TextFormatter.Change> getUnaryOperator() {
        UnaryOperator<Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?([1-9][0-9]*)?")) { return change; }
            else if ("-".equals(change.getText()) )
            {
            	if (change.getControlText().startsWith("-"))
                {
                    change.setText("");
                    change.setRange(0, 1);
                    change.setCaretPosition(change.getCaretPosition()-2);
                    change.setAnchor(change.getAnchor()-2);
                }
                else { change.setRange(0, 0); }
                return change ;
            }
            return null;
        };
        
        return integerFilter;
    }
}
