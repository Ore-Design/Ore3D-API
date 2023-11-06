package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Jackson.SpecSerialization;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import lombok.Getter;

@JsonSerialize(using = SpecSerialization.IntStringMapSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.IntStringMapSerialization.Deserializer.class)
public class IntegerStringMapSpec extends Spec<Integer>
{
	public IntegerStringMapSpec(String id, String mapID, Integer initialValue, boolean readOnly, String section)
	{
		super(id, new SimpleIntegerProperty(initialValue).asObject(), readOnly, section);
		
		if(!Util.getRegisteredIntegerStringMaps().containsKey(mapID)) throw new IllegalArgumentException("No registered map exits with ID " + mapID + "!");
		else this.mapID = mapID;
	}
	
	@Getter String mapID;
	
	@Override
	public void setValue(Integer val)
	{
		if(readOnly) return;
		Map<Integer, String> matchingMap = Util.getRegisteredIntegerStringMaps().get(mapID);
		
		if(matchingMap == null) throw new NullPointerException("No registered map exits with ID " + mapID + "!");
		else
		{
			if(matchingMap.containsKey(val)) property.setValue(val);
			else throw new IllegalArgumentException("No matching value exists in " + mapID + " for value " + val + "!");
		}
	}
	
	@Override
	public Pane getUI(List<Spec<?>> toBind)
	{
		Map<Integer, String> matchingMap = Util.getRegisteredIntegerStringMaps().get(mapID);
		if(matchingMap == null) throw new NullPointerException("No registered map exits with ID " + mapID + "!");
		
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
		ChoiceBox<Integer> dropdown = new ChoiceBox<>();
		dropdown.getItems().setAll(matchingMap.keySet());
		// This converter makes the multiselect appear as dash, and converts from integer value to string display
		dropdown.setConverter(new StringConverter<Integer>()
		{
			@Override
			public String toString(Integer object)
			{
				if(object == null) return "-";
				else return matchingMap.get(object);
			}

			@Override
			public Integer fromString(String string)
			{
				for(Entry<Integer, String> entry : matchingMap.entrySet()) { if(entry.getValue().equals(string)) return entry.getKey(); }
				
				return 0;
			}
		});
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
