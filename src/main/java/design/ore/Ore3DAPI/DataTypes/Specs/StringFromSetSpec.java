package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.Set;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public class StringFromSetSpec extends Spec<String>
{
	public StringFromSetSpec(String id, Set<String> valueSet, String initialValue, boolean readOnly, String section)
	{ super(id, new SimpleStringProperty(initialValue), readOnly, section); this.valueSet = valueSet; }
	
	Set<String> valueSet;
	
	@Override
	public Pane getUI(List<Property<?>> toBind)
	{
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
		ChoiceBox<String> dropdown = new ChoiceBox<>();
		dropdown.getItems().setAll(valueSet);
		// This converter makes the multiselect appear as dash
		dropdown.setConverter(new StringConverter<String>()
		{
			@Override
			public String toString(String object)
			{
				if(object == null) return "-";
				else return object.toString();
			}

			@Override
			public String fromString(String string)
			{
				return string;
			}
		});
		if(readOnly) dropdown.setDisable(true);
		
		if(toBind != null && toBind.size() > 0)
		{
			try
			{
				String firstVal = (String) toBind.get(0).getValue();
				for(int x = 1 ; x < toBind.size() ; x++)
				{
					try
					{
						String nextVal = (String) toBind.get(x).getValue();
						if(firstVal != nextVal)
						{
							dropdown.getSelectionModel().clearSelection();
							firstVal = null;
							break;
						}
					} catch(Exception e) { dropdown.getSelectionModel().clearSelection(); firstVal = null; }
				}
				if(firstVal != null) dropdown.setValue(firstVal);
			}
			catch (Exception e) { dropdown.getSelectionModel().clearSelection(); }
			
			dropdown.valueProperty().addListener(l ->
			{
				toBind.forEach(p -> { ((Property<String>)p).setValue(dropdown.getValue()); });
			});
		}
		else
		{
			dropdown.valueProperty().bindBidirectional(property);
		}
		
		HBox input = new HBox(idLabel, dropdown);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.5));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		dropdown.prefWidthProperty().bind(input.widthProperty().multiply(0.5));
		dropdown.setMaxWidth(Control.USE_PREF_SIZE);
		
		input.setPrefHeight(25);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
}
