package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.controlsfx.control.SearchableComboBox;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public class SearchableIntegerStringMapSpec extends Spec<Integer>
{
	public SearchableIntegerStringMapSpec(String id, Map<Integer, String> valueSet, Integer initialValue, boolean readOnly, String section)
	{
		super(id, new SimpleIntegerProperty(initialValue).asObject(), readOnly, section);
		this.valueSet = valueSet;
	}
	
	Map<Integer, String> valueSet;
	
	@Override
	public void setValue(Integer val)
	{
		if(!readOnly && valueSet.containsKey(val)) property.setValue(val);
	}
	
	@Override
	public Pane getUI(List<Property<?>> toBind)
	{
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
		StringConverter<Integer> converter = new StringConverter<Integer>()
		{
			@Override
			public String toString(Integer object)
			{
				if(object == null || !valueSet.containsKey(object)) return "-";
				else return valueSet.get(object);
			}

			@Override
			public Integer fromString(String string)
			{
				for(Entry<Integer, String> entry : valueSet.entrySet()) { if(entry.getValue().equals(string)) return entry.getKey(); }
				
				return 0;
			}
		};
		
		SearchableComboBox<Integer> dropdown = new SearchableComboBox<>();
		dropdown.getItems().setAll(valueSet.keySet());
		dropdown.setMinHeight(0);
		// This converter makes the multiselect appear as dash, and converts from integer value to string display
		dropdown.setConverter(converter);
		if(readOnly) dropdown.setDisable(true);
		
		if(toBind != null && toBind.size() > 0)
		{
			try
			{
				Integer firstVal = (Integer) toBind.get(0).getValue();
				for(int x = 1 ; x < toBind.size() ; x++)
				{
					try
					{
						Integer nextVal = (Integer) toBind.get(x).getValue();
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
				toBind.forEach(p -> { ((Property<Integer>)p).setValue(dropdown.getValue()); });
			});
		}
		else
		{
			dropdown.valueProperty().bindBidirectional(property);
		}
		
		HBox input = new HBox(idLabel, dropdown);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		dropdown.prefWidthProperty().bind(input.widthProperty().multiply(0.6));
		dropdown.setMaxWidth(Control.USE_PREF_SIZE);
		
		input.setPrefHeight(25);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
}
