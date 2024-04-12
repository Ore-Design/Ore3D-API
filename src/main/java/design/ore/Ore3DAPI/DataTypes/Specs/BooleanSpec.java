package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

@JsonSerialize(using = SpecSerialization.BooleanSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.BooleanSerialization.Deserializer.class)
public class BooleanSpec extends Spec<Boolean>
{
	public BooleanSpec(Build parent, String id, boolean initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }

	public BooleanSpec(Build parent, String id, boolean initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public BooleanSpec(Build parent, String id, boolean initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Boolean> calculateOnDirty)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty); }
	
	public BooleanSpec(Build parent, String id, boolean initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, Callable<Boolean> calculateOnDirty)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null); }
	
	public BooleanSpec(Build parent, String id, boolean initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Boolean> calculateOnDirty, String uniqueBehaviorNotifier)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }
	
	public BooleanSpec(Build parent, String id, boolean initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, Callable<Boolean> calculateOnDirty, String uniqueBehaviorNotifier)
	{ super(parent, id, new SimpleBooleanProperty(initialValue), readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }

	@Override
	public Pane getUI(List<Spec<?>> toBind, String popoutID)
	{
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
		if(uniqueBehaviorNotifierProperty.isNotNull().get() && uniqueBehaviorNotifierProperty.isNotEmpty().get())
		{
			idLabel.getStyleClass().add("italic-spec-label");
			idLabel.setText(idLabel.getText() + "*");
			idLabel.setTooltip(new Tooltip(uniqueBehaviorNotifierProperty.get()));
		}
		
		CheckBox check = new CheckBox();
		check.disableProperty().bind(readOnlyProperty.or(Bindings.createBooleanBinding(() -> parent.parentIsExpired())));
		
		if(toBind != null && toBind.size() > 0)
		{
			try
			{
				boolean firstVal = (boolean) toBind.get(0).getValue();
				for(int x = 1 ; x < toBind.size() ; x++)
				{
					try
					{
						boolean nextVal = (boolean) toBind.get(x).getValue();
						if(firstVal != nextVal)
						{
							check.setIndeterminate(true);
							break;
						}
					} catch(Exception e) { check.setIndeterminate(true); }
				}
				check.setSelected(firstVal);
			}
			catch (Exception e) { check.setIndeterminate(true); }
			
			check.selectedProperty().addListener(l ->
			{
				toBind.forEach(p -> { ((Property<Boolean>)p).setValue(check.isSelected()); });
			});
		}
		else
		{
			check.selectedProperty().bindBidirectional(valueProperty);
		}
		
		HBox input = new HBox(idLabel, check);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		check.setMaxWidth(Double.MAX_VALUE);
		Util.UI.checkboxMatchSize(check);
		HBox.setHgrow(check, Priority.ALWAYS);
		
		input.setPrefHeight(20);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
}
