package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.JavaFX.NonNullDoubleStringConverter;
import design.ore.Ore3DAPI.UI.ToggleIconButton;
import design.ore.Ore3DAPI.Util.Log;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import lombok.Getter;

@JsonSerialize(using = SpecSerialization.LinkedDoubleSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.LinkedDoubleSerialization.Deserializer.class)
public class LinkedDoubleSpec extends Spec<Number>
{
	public LinkedDoubleSpec(Build parent, String id, double initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public LinkedDoubleSpec(Build parent, String id, double initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty); }
	
	public LinkedDoubleSpec(Build parent, String id, double initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null, false, null); }
	
	public LinkedDoubleSpec(Build parent, String id, double initialValue, boolean readOnly, String section, boolean countsAsMatch, LinkedDoubleSpec toLink, boolean linked)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, null, toLink, linked, null); }
	
	public LinkedDoubleSpec(Build parent, String id, double initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty, LinkedDoubleSpec toLink, boolean linked)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, toLink, linked, null); }
	
	public LinkedDoubleSpec(Build parent, String id, double initialValue, boolean readOnly, String section, boolean countsAsMatch, LinkedDoubleSpec toLink, boolean linked, String uniqueBehaviorNotifier)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, null, toLink, linked, uniqueBehaviorNotifier); }
	
	public LinkedDoubleSpec(Build parent, String id, double initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty, LinkedDoubleSpec toLink, boolean linked, String uniqueBehaviorNotifier)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, toLink, linked, uniqueBehaviorNotifier); }
	
	public LinkedDoubleSpec(Build parent, String id, double initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty, LinkedDoubleSpec toLink, boolean linked, String uniqueBehaviorNotifier)
	{
		super(parent, id, new SimpleDoubleProperty(initialValue), readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier);
		
		linkedSpecProperty.addListener((obs, oldVal, newVal) ->
		{
			if(newVal != null)
			{
				newVal.linkedSpecProperty.set(this);
				this.linkActiveProperty.bindBidirectional(newVal.getLinkActiveProperty());
				linkActiveProperty.addListener((obs1, oldVal1, newVal1) ->
				{
					if(newVal1) this.valueProperty.bindBidirectional(newVal.valueProperty);
					else this.valueProperty.unbindBidirectional(newVal.valueProperty);
				});
				linkActiveProperty.setValue(linked);
			}
		});
		
		if(toLink != null) linkedSpecProperty.set(toLink);
		linkActiveProperty.set(linked);
	}
	
	public double getDoubleValue() { return valueProperty.getValue().doubleValue(); }
	
	private ObjectProperty<LinkedDoubleSpec> linkedSpecProperty = new SimpleObjectProperty<>();
	
	@Getter private BooleanProperty linkActiveProperty = new SimpleBooleanProperty();
	private String preEdit = "";
	
	public void link(LinkedDoubleSpec s) { this.linkedSpecProperty.set(s); }

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
		
		ToggleIconButton button = new ToggleIconButton(
			Util.UI.colorize(new ImageView(Util.getChainIcon()), Util.Colors.getAccent()),
			Util.UI.colorize(new ImageView(Util.getBrokenChainIcon()), Util.Colors.getAccent()),
			true, linkActiveProperty);
		button.setOnAction(e -> linkActiveProperty.setValue(!linkActiveProperty.get()));
		button.disableProperty().bind(readOnlyProperty.or(Bindings.createBooleanBinding(() -> parent.parentIsExpired())));
		
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
			        else { if(inputField.getText().equals("")) inputField.setText("0"); }
			    }
			});
			
			String firstVal = "";
			try
			{
				firstVal = ((Number) toBind.get(0).getValue()) + "";
				for(int x = 1 ; x < toBind.size() ; x++)
				{
					String nextVal = "";
					try { nextVal = (Number) toBind.get(x).getValue() + ""; } catch(Exception e) {}
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
					Number val = text.equals("") ? 0.0 : Double.parseDouble(text);
					toBind.forEach(p ->
					{
						if(p instanceof LinkedDoubleSpec) ((LinkedDoubleSpec) p).setValue(val);
						else Log.getLogger().warn("Non-LinkedDoubleSpec passed into LinkedDoubleSpec multiselect!");
					});
				}
				catch(Exception e) { inputField.setText(preEdit); } });
		}
		else
		{
			inputField.setTextFormatter(Util.getDecimalFormatter(4));
			inputField.textProperty().bindBidirectional(this.valueProperty, new NonNullDoubleStringConverter());
			inputField.focusedProperty().addListener(new ChangeListener<Boolean>()
			{
			    @Override
			    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
			    {
			        if (!newPropertyValue) { if(inputField.getText().equals("")) inputField.setText("0.0"); }
			    }
			});
		}
		
		
		HBox input = null;
		if(linkedSpecProperty.isNotNull().get())
		{
			input = new HBox(idLabel, button, inputField);
			idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.4));
			idLabel.setMaxWidth(Control.USE_PREF_SIZE);
			button.prefWidthProperty().bind(input.widthProperty().multiply(0.1));
			button.setMaxWidth(Control.USE_PREF_SIZE);
			inputField.prefWidthProperty().bind(input.widthProperty().multiply(0.5));
			inputField.setMaxWidth(Control.USE_PREF_SIZE);
		}
		else
		{
			input = new HBox(idLabel, inputField);
			idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.4));
			idLabel.setMaxWidth(Control.USE_PREF_SIZE);
			inputField.prefWidthProperty().bind(input.widthProperty().multiply(0.6));
			inputField.setMaxWidth(Control.USE_PREF_SIZE);
		}
		
		input.setAlignment(Pos.CENTER_LEFT);
		input.setPrefHeight(20);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
}
