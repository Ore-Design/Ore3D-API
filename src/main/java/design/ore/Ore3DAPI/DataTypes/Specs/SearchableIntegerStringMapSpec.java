package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Registry;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.UI.SearchableDropdown;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import lombok.Getter;

@JsonSerialize(using = SpecSerialization.SearchableIntStringMapSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.SearchableIntStringMapSerialization.Deserializer.class)
public class SearchableIntegerStringMapSpec extends Spec<Integer>
{
	public SearchableIntegerStringMapSpec(Build parent, String id, String mapID, Integer initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, mapID, initialValue, readOnly, section, countsAsMatch, null); }
	
	public SearchableIntegerStringMapSpec(Build parent, String id, String mapID, Integer initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Integer> calculateOnDirty, String uniqueBehaviorNotifier)
	{ this(parent, id, mapID, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }
	
	public SearchableIntegerStringMapSpec(Build parent, String id, String mapID, Integer initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Integer> calculateOnDirty)
	{ this(parent, id, mapID, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, null); }
	
	public SearchableIntegerStringMapSpec(Build parent, String id, String mapID, Integer initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, Callable<Integer> calculateOnDirty, String uniqueBehaviorNotifier)
	{
		super(parent, id, new SimpleIntegerProperty(initialValue).asObject(), readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier);
		
		if(!Registry.getRegisteredIntegerStringMaps().containsKey(mapID)) throw new IllegalArgumentException("No registered map exits with ID " + mapID + "!");
		else this.mapID = mapID;
	}
	
	@Getter String mapID;
	public String getStringValue() { return Registry.getRegisteredIntegerStringMaps().get(mapID).get(getValue()); }
	
	@Override
	public void setValue(Integer val)
	{
		Map<Integer, String> matchingMap = Registry.getRegisteredIntegerStringMaps().get(mapID);
		
		if(matchingMap == null) throw new NullPointerException("No registered map exits with ID " + mapID + "!");
		else
		{
			if(matchingMap.containsKey(val) || val == null) valueProperty.setValue(val);
			else throw new IllegalArgumentException("No matching value exists in " + mapID + " for value " + val + "!");
		}
	}
	
	@Override
	public Pane getUI(List<Spec<?>> toBind, String popoutID)
	{
		Map<Integer, String> matchingMap = Registry.getRegisteredIntegerStringMaps().get(mapID);
		if(matchingMap == null) throw new NullPointerException("No registered map exits with ID " + mapID + "!");
		
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
		if(uniqueBehaviorNotifierProperty.isNotNull().get() && uniqueBehaviorNotifierProperty.isNotEmpty().get())
		{
			idLabel.getStyleClass().add("italic-spec-label");
			idLabel.setText(idLabel.getText() + "*");
			idLabel.setTooltip(new Tooltip(uniqueBehaviorNotifierProperty.get()));
		}
		
		StringConverter<Integer> converter = new StringConverter<Integer>()
		{
			@Override
			public String toString(Integer object)
			{
				if(object == null || !matchingMap.containsKey(object)) return "-";
				else return matchingMap.get(object);
			}

			@Override
			public Integer fromString(String string)
			{
				for(Entry<Integer, String> entry : matchingMap.entrySet()) { if(entry.getValue().equals(string)) return entry.getKey(); }
				
				return 0;
			}
		};
		
		SearchableDropdown<Integer> dropdown = new SearchableDropdown<>();
		dropdown.getItems().setAll(matchingMap.keySet());
		dropdown.setMinHeight(0);
		// This converter makes the multiselect appear as dash, and converts from integer value to string display
		dropdown.setConverter(converter);
		dropdown.disableProperty().bind(readOnlyProperty.or(Bindings.createBooleanBinding(() -> parent.parentIsExpired())));
		dropdown.setCellFactory(listView -> new ListCell<Integer>()
		{
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if(empty)
				{
					setDisable(true);
					setText("");
				}
				else
				{
					setText(converter.toString(item));
					setDisable(false);
				}
			}
		});
		
//		final ObjectProperty<Integer> selectedDropdownItem = new SimpleObjectProperty<Integer>(getValue());
		
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
							dropdown.clearSelection();
							firstVal = null;
							break;
						}
					} catch(Exception e) { dropdown.clearSelection(); firstVal = null; }
				}
				if(firstVal != null) dropdown.setValue(firstVal);
			}
			catch (Exception e) { dropdown.clearSelection(); }
			
			dropdown.valueProperty().addListener((obs, oldVal, newVal) ->
			{
				if(newVal != null)
				{
					toBind.forEach(p ->
					{
						if(p instanceof SearchableFilteredIntegerStringMapSpec) ((SearchableFilteredIntegerStringMapSpec) p).setValue(dropdown.getValue());
						else Log.getLogger().warn("Non-SearchableFilteredIntegerStringMapSpec passed into SearchableFilteredIntegerStringMapSpec multiselect!");
					});
				}
				Log.getLogger().debug("Dropdown update from multi spec!");
			});
		}
		else { dropdown.valueProperty().bindBidirectional(valueProperty); }
		
		HBox input = new HBox(idLabel, dropdown);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		dropdown.prefWidthProperty().bind(input.widthProperty().multiply(0.6));
		dropdown.setMaxWidth(Control.USE_PREF_SIZE);
		
		input.setPrefHeight(20);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
}
