package design.ore.Ore3DAPI.data.specs.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.Ore3DAPI.data.interfaces.ISpecUI;
import design.ore.Ore3DAPI.data.specs.SearchableMapSpec;
import design.ore.Ore3DAPI.data.specs.Spec;
import design.ore.Ore3DAPI.ui.SearchableDropdown;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

public class SearchableMapSpecUI<T, V> extends HBox implements ISpecUI<T>
{
	private final SearchableMapSpec<T, V> parentSpec;
	
	private final SearchableDropdown<T> dropdown;
	private final Label idLabel;
	
	protected final ChangeListener<String> uniqueBehaviorIdentifierListener;
	protected final ChangeListener<Predicate<T>> filterChangeListener;
	protected final ChangeListener<Map<T, V>> mapChangeListener;
	protected ChangeListener<T> multiChangeListener;
	
	public SearchableMapSpecUI(SearchableMapSpec<T, V> parent)
	{
		parentSpec = parent;
		
		idLabel = new Label(parentSpec.getId());
		idLabel.getStyleClass().add("spec-label");
		idLabel.prefWidthProperty().bind(widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		
		dropdown = new SearchableDropdown<>();
		dropdown.setCellFactory((listView) -> new ListCell<>()
		{
			@Override
			public void updateItem(T item, boolean empty)
			{
				super.updateItem(item, empty);
				if(empty || item == null)
				{
					setText("");
					setDisable(true);
					setTooltip(null);
				}
				else
				{
					setText(parentSpec.getMap().get().get(item).toString());
					setDisable(false);
					setTooltip(new Tooltip(parentSpec.getMap().get().get(item).toString()));
				}
			}
		});
		
		// This converter makes the multiselect appear as dash, and converts from integer value to string display
		dropdown.setConverter(new StringConverter<T>()
		{
			@Override
			public String toString(T object)
			{
				if(object == null || parentSpec.getMap().getValue() == null) return "-";
				else return parentSpec.getMap().getValue().get(object).toString();
			}

			@Override
			public T fromString(String string)
			{
				if(parentSpec.getMap().getValue() == null) return null;
				
				for(Entry<T, V> entry : parentSpec.getMap().getValue().entrySet()) { if(entry.getValue().toString().equals(string)) return entry.getKey(); }
				
				return null;
			}
		});
		dropdown.prefWidthProperty().bind(widthProperty().multiply(0.6));
		dropdown.setMaxWidth(Control.USE_PREF_SIZE);
		
		getChildren().addAll(idLabel, dropdown);
		setAlignment(Pos.CENTER_LEFT);
		setPrefHeight(20);
		setMaxHeight(Control.USE_PREF_SIZE);

		uniqueBehaviorIdentifierListener = (obs, oldVal, newVal) ->
		{
			if(newVal != null) formatLabel(newVal);
			else formatLabel("");
		};
		
		filterChangeListener = (obs, oldVal, newVal) -> dropdown.getItems().setAll(filter(parentSpec.getMap().getValue() != null ? parentSpec.getMap().getValue().keySet() : new ArrayList<>(), newVal));
		mapChangeListener = (obs, oldVal, newVal) ->
		{
			if(newVal != null) dropdown.getItems().setAll(filter(newVal.keySet(), parentSpec.getFilter()));
			else dropdown.getItems().clear();
		};
	}
	
	private Collection<T> filter(Collection<T> originalCollection, Predicate<T> filter)
	{
		if(originalCollection == null) return new ArrayList<>();
		else
		{
			if(filter != null) return originalCollection.stream().filter(parentSpec.getFilter()).toList();
			else return originalCollection;
		}
	}

	@Override
	public Node getUINode() { return this; }

	@Override
	public void unbindUI()
	{
		parentSpec.getFilterProperty().removeListener(filterChangeListener);
		parentSpec.getMap().removeListener(mapChangeListener);
		dropdown.disableProperty().unbind();
		parentSpec.getUniqueBehaviorNotifierProperty().removeListener(uniqueBehaviorIdentifierListener);
		if(multiChangeListener != null) dropdown.valueProperty().removeListener(multiChangeListener);
		dropdown.valueProperty().unbindBidirectional(parentSpec);
		dropdown.valueProperty().addListener(multiChangeListener);
		dropdown.getItems().clear();
	}

	@Override
	public void rebindUI(String popoutID)
	{
		parentSpec.getFilterProperty().addListener(filterChangeListener);
		parentSpec.getMap().addListener(mapChangeListener);
		dropdown.getItems().setAll(filter(parentSpec.getMap().getValue() != null ? parentSpec.getMap().getValue().keySet() : new ArrayList<>(), parentSpec.getFilter()));
		
		dropdown.disableProperty().bind(parentSpec.getReadOnlyProperty().or(Bindings.createBooleanBinding(() -> parentSpec.getParentBuild().parentIsExpired())));
		dropdown.valueProperty().bindBidirectional(parentSpec);

		if(parentSpec.getUniqueBehaviorNotifierProperty().isNotEmpty().get()) formatLabel(parentSpec.getUniqueBehaviorNotifierProperty().get());
		else formatLabel("");
		parentSpec.getUniqueBehaviorNotifierProperty().addListener(uniqueBehaviorIdentifierListener);
	}
	
	@Override
	public void rebindMultiUI(List<Spec<T>> specs, String popoutID)
	{
		parentSpec.getFilterProperty().addListener(filterChangeListener);
		parentSpec.getMap().addListener(mapChangeListener);
		dropdown.getItems().setAll(filter(parentSpec.getMap().getValue() != null ? parentSpec.getMap().getValue().keySet() : new ArrayList<>(), parentSpec.getFilter()));
		
		T firstVal = null;
		try
		{
			firstVal = specs.get(0).getValue();
			for(int x = 1 ; x < specs.size() ; x++)
			{
				T nextVal = null;
				try { nextVal = specs.get(x).getValue(); } catch(Exception e) {}
				if(!firstVal.equals(nextVal))
				{
					firstVal = null;
					break;
				}
			}
		}
		catch (Exception e) {}
		
		
		dropdown.setValue(firstVal);
		
		multiChangeListener = (obs, oldVal, newVal) -> specs.forEach(s -> s.setValue(newVal));
		dropdown.valueProperty().addListener(multiChangeListener);
	}
	
	private void formatLabel(String uniqueBehaviorNotice)
	{
		if(uniqueBehaviorNotice.equals(""))
		{
			idLabel.getStyleClass().remove("italic-spec-label");
			idLabel.setText(parentSpec.getId());
			idLabel.setTooltip(null);
		}
		else
		{
			idLabel.getStyleClass().add("italic-spec-label");
			idLabel.setText(parentSpec.getId() + "*");
			idLabel.setTooltip(new Tooltip(uniqueBehaviorNotice));
		}
	}

	private boolean bound = false;
	@Override @JsonIgnore public boolean isBound() { return bound; }
	@Override @JsonIgnore public void setBound() { bound = true; }
}
