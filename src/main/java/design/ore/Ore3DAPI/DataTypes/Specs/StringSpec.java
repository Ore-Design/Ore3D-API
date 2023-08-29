package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class StringSpec extends Spec<String>
{
	public StringSpec(String id, String initialValue, boolean readOnly, String section) { super(id, new SimpleStringProperty(initialValue), readOnly, section); }

	@Override
	public Pane getUI(List<Property<?>> toBind)
	{
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
		TextField inputField = new TextField(property.getValue());
		inputField.getStyleClass().add("spec-text-field");
		if(readOnly) inputField.setDisable(true);
		
		if(toBind != null && toBind.size() > 0)
		{
			String firstVal = "";
			try
			{
				firstVal = (String) toBind.get(0).getValue();
				for(int x = 1 ; x < toBind.size() ; x++)
				{
					String nextVal = "";
					try { nextVal = (String) toBind.get(x).getValue(); } catch(Exception e) {}
					if(!firstVal.equals(nextVal))
					{
						firstVal = "-";
						break;
					}
				}
			}
			catch (Exception e) {}
			inputField.setText(firstVal);
			inputField.textProperty().addListener(l ->
			{
				toBind.forEach(p -> { try { ((Property<String>)p).setValue(inputField.getText()); } catch(Exception e) {} });
			});
		}
		else inputField.textProperty().bindBidirectional(property);
		
		HBox input = new HBox(idLabel, inputField);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.5));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		inputField.prefWidthProperty().bind(input.widthProperty().multiply(0.5));
		inputField.setMaxWidth(Control.USE_PREF_SIZE);
		
		input.setPrefHeight(25);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
}
