package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Registry;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.UI.SearchableDropdown;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import lombok.Getter;

@JsonSerialize(using = SpecSerialization.SearchableFilteredIntStringMapSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.SearchableFilteredIntStringMapSerialization.Deserializer.class)
public class SearchableFilteredIntegerStringMapSpec extends Spec<Integer>
{
	public SearchableFilteredIntegerStringMapSpec(Build parent, String id, String mapID, Integer initialValue, boolean readOnly, String section, boolean countsAsMatch, ObjectBinding<Predicate<Integer>> filterPredicate)
	{ this(parent, id, mapID, initialValue, readOnly, section, countsAsMatch, null, filterPredicate); }
	
	public SearchableFilteredIntegerStringMapSpec(Build parent, String id, String mapID, Integer initialValue, boolean readOnly, String section, boolean countsAsMatch,
		Callable<Integer> calculateOnDirty, ObjectBinding<Predicate<Integer>> filterPredicate)
	{ this(parent, id, mapID, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, filterPredicate, null); }
	
	public SearchableFilteredIntegerStringMapSpec(Build parent, String id, String mapID, Integer initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch,
		Callable<Integer> calculateOnDirty, ObjectBinding<Predicate<Integer>> filterPredicate)
	{ this(parent, id, mapID, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, filterPredicate, null); }
	
	public SearchableFilteredIntegerStringMapSpec(Build parent, String id, String mapID, Integer initialValue, boolean readOnly, String section, boolean countsAsMatch,
		Callable<Integer> calculateOnDirty, ObjectBinding<Predicate<Integer>> filterPredicate, String uniqueBehaviorNotifier)
	{ this(parent, id, mapID, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, filterPredicate, uniqueBehaviorNotifier); }
	
	public SearchableFilteredIntegerStringMapSpec(Build parent, String id, String mapID, Integer initialValue, ObservableBooleanValue readOnly, String section,
		boolean countsAsMatch, Callable<Integer> calculateOnDirty, ObjectBinding<Predicate<Integer>> filterPredicate, String uniqueBehaviorNotifier)
	{
		super(parent, id, initialValue == null ? new SimpleIntegerProperty().asObject() : new SimpleIntegerProperty(initialValue).asObject(), readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier);
		
		if(filterPredicate == null) this.filterPredicate = Bindings.createObjectBinding(() -> null);
		else this.filterPredicate = filterPredicate;
		
		if(!Registry.getRegisteredIntegerStringMaps().containsKey(mapID)) throw new IllegalArgumentException("No registered map exits with ID " + mapID + "!");
		else this.mapID = mapID;
	}
	
	@Getter String mapID;
	public String getStringValue() { return Registry.getRegisteredIntegerStringMaps().get(mapID).get(getValue()); }
	
	ObjectBinding<Predicate<Integer>> filterPredicate;
	
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
		FilteredList<Integer> list = new FilteredList<>(FXCollections.observableArrayList(matchingMap.keySet()));
		list.setPredicate(filterPredicate.get());
		filterPredicate.addListener((obs, oldVal, newVal) ->
		{
			valueProperty.setValue(null);
			Platform.runLater(() ->
			{
				dropdown.setItems(FXCollections.observableArrayList());
				list.setPredicate(newVal);
				dropdown.setItems(list);
			});
		});
		dropdown.setItems(list);
		dropdown.setMinHeight(10);
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
		
		final ObjectProperty<Integer> selectedDropdownItem = new SimpleObjectProperty<Integer>(getValue());
		
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
			
			dropdown.valueProperty().addListener((obs, oldVal, newVal) -> selectedDropdownItem.setValue(newVal));
			
			selectedDropdownItem.addListener((obs, oldVal, newVal) ->
			{
				if(newVal != null)
				{
					toBind.forEach(p ->
					{
						if(p instanceof SearchableFilteredIntegerStringMapSpec) ((SearchableFilteredIntegerStringMapSpec) p).setValue(dropdown.getValue());
						else Log.getLogger().warn("Non-SearchableFilteredIntegerStringMapSpec passed into SearchableFilteredIntegerStringMapSpec multiselect!");
					});
				}
			});
		}
		else
		{
			dropdown.valueProperty().bindBidirectional(selectedDropdownItem);
			selectedDropdownItem.addListener((obs, oldVal, newVal) -> { if(newVal != null) valueProperty.setValue(newVal); });
		}
		
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
