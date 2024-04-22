package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.JavaFX.NonNullIntegerStringConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import lombok.Getter;

@JsonSerialize(using = SpecSerialization.PositiveIntSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.PositiveIntSerialization.Deserializer.class)
public class PositiveIntSpec extends Spec<Integer>
{
	public PositiveIntSpec(Build parent, String id, int initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public PositiveIntSpec(Build parent, String id, int initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, Callable<Integer> calculateOnDirty)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null); }
	
	public PositiveIntSpec(Build parent, String id, int initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Integer> calculateOnDirty)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, null); }

	public PositiveIntSpec(Build parent, String id, int initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null, null); }

	public PositiveIntSpec(Build parent, String id, int initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, String uniqueBehaviorNotifier)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null, uniqueBehaviorNotifier); }
	
	public PositiveIntSpec(Build parent, String id, int initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, Callable<Integer> calculateOnDirty, String uniqueBehaviorNotifier)
	{
		super(parent, id, new SimpleIntegerProperty(initialValue).asObject(), readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier);
		intProperty.bind(valueProperty);
	}
	
	@Getter private IntegerProperty intProperty = new SimpleIntegerProperty();
	public ObservableNumberValue getNumberProperty() { return intProperty; }
	public void bindBidirectional(StringProperty other, StringConverter<Integer> converter) { other.bindBidirectional(valueProperty, converter); }
	
	private String preEdit = "";

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
		
		TextField inputField = new TextField();
		inputField.getStyleClass().add("spec-text-field");
		inputField.disableProperty().bind(readOnlyProperty.or(Bindings.createBooleanBinding(() -> parent.parentIsExpired())));
		
		if(toBind != null && toBind.size() > 0)
		{
			inputField.focusedProperty().addListener(new ChangeListener<Boolean>()
			{
			    @Override
			    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
			    {
			        if (newPropertyValue) { preEdit = inputField.getText(); }
			        else { if(inputField.getText().equals("")) inputField.setText("1"); }
			    }
			});
			
			String firstVal = "";
			try
			{
				firstVal = ((int) toBind.get(0).getValue()) + "";
				for(int x = 1 ; x < toBind.size() ; x++)
				{
					String nextVal = "";
					try { nextVal = (int) toBind.get(x).getValue() + ""; } catch(Exception e) {}
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
				try
				{
					String text = inputField.getText();
					int val = text.equals("") ? 1 : Integer.parseInt(inputField.getText());
					toBind.forEach(p ->
					{
						if(p instanceof PositiveIntSpec) ((PositiveIntSpec) p).setValue(val);
						else Log.getLogger().warn("Non-PositiveIntSpec passed into PositiveIntSpec multiselect!");
					});
				}
				catch(Exception e) { inputField.setText(preEdit); } });
		}
		else
		{
			final ChangeListener<Boolean> avoidEmpty = (obs, oldVal, newVal) -> { if (!newVal) { if(inputField.getText().equals("")) inputField.setText("0.0"); } };
			final ChangeListener<Boolean> calculateOnEnd = (obs, oldVal, newVal) ->
			{
				if (!newVal)
				{
					if(inputField.getText().equals(""))
					{
						inputField.setText("0.0");
						valueProperty.setValue(0);
					}
					else valueProperty.setValue(Integer.parseInt(inputField.getText()));
				}
			};
			final ChangeListener<Number> updateFieldOnValueChange = (obs, oldVal, newVal) -> { if (newVal != null) { inputField.textProperty().setValue(getValue() + ""); } };
			
			inputField.setTextFormatter(Util.getDecimalFormatter(4));
			if(holdCalculateTillCompleteProperty.getValue() != null) { setHoldCalculateTillCompleteBindings(holdCalculateTillCompleteProperty.getValue(), inputField, avoidEmpty, calculateOnEnd, updateFieldOnValueChange); }
			holdCalculateTillCompleteProperty.addListener((observable, oldValue, newValue) -> { if(newValue != null) { setHoldCalculateTillCompleteBindings(newValue, inputField, avoidEmpty, calculateOnEnd, updateFieldOnValueChange); } });
		}
		
		HBox input = new HBox(idLabel, inputField);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		inputField.prefWidthProperty().bind(input.widthProperty().multiply(0.6));
		inputField.setMaxWidth(Control.USE_PREF_SIZE);
		
		input.setPrefHeight(20);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
	
	private void setHoldCalculateTillCompleteBindings(boolean hold, TextField inputField, final ChangeListener<Boolean> avoidEmpty, final ChangeListener<Boolean> calculateOnEnd, final ChangeListener<Number> updateFieldOnValueChange)
	{
		if(hold)
		{
			inputField.focusedProperty().removeListener(avoidEmpty);
			
			inputField.textProperty().unbindBidirectional(this.valueProperty);
			inputField.textProperty().setValue(getValue() + "");
			
			inputField.focusedProperty().addListener(calculateOnEnd);
			valueProperty.addListener(updateFieldOnValueChange);
		}
		else
		{
			inputField.focusedProperty().removeListener(calculateOnEnd);
			valueProperty.removeListener(updateFieldOnValueChange);
			
			inputField.textProperty().bindBidirectional(this.valueProperty, new NonNullIntegerStringConverter());
			
			inputField.focusedProperty().addListener(avoidEmpty);
		}
	}
}
