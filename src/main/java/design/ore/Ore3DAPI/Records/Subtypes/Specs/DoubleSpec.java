package design.ore.Ore3DAPI.Records.Subtypes.Specs;

import design.ore.Ore3DAPI.JavaFX.DecimalTextFormatter;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.converter.DoubleStringConverter;

public class DoubleSpec extends Spec<Double>
{
	public DoubleSpec(String id, double initialValue, boolean readOnly, String section) { super(id, new SimpleDoubleProperty(initialValue).asObject(), readOnly, section); }

	@Override
	public Pane getUI()
	{
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
		TextField inputField = new TextField(id);
		inputField.textProperty().bindBidirectional(this.value, new DoubleStringConverter());
		inputField.setTextFormatter(new DecimalTextFormatter(0, 4));
		inputField.getStyleClass().add("spec-text-field");
		if(readOnly) inputField.setDisable(true);
		
		HBox input = new HBox(idLabel, inputField);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.5));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		inputField.prefWidthProperty().bind(input.widthProperty().multiply(0.5));
		inputField.setMaxWidth(Control.USE_PREF_SIZE);
		
		input.setPrefHeight(20);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
}
