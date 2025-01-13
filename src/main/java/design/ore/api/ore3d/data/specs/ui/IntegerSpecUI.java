package design.ore.api.ore3d.data.specs.ui;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.api.ore3d.Util;
import design.ore.api.ore3d.data.interfaces.ISpecUI;
import design.ore.api.ore3d.data.specs.IntegerSpec;
import design.ore.api.ore3d.data.specs.Spec;
import design.ore.api.ore3d.javafx.NonNullIntegerStringConverter;
import design.ore.api.ore3d.javafx.PositiveIntegerTextFormatter;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class IntegerSpecUI extends HBox implements ISpecUI<Number>
{
	private final IntegerSpec parentSpec;
	
	private final TextField inputField;
	private final Label idLabel;
	
	private final ChangeListener<String> uniqueBehaviorIdentifierListener;
	private final ChangeListener<Boolean> holdSetTillDoneListener;
	private final ChangeListener<Boolean> avoidEmptyListener;
	private final ChangeListener<Boolean> calculateOnEndListener;
	private final ChangeListener<Number> updateFieldOnValueChangeListener;
	private final ChangeListener<Boolean> positiveOnlyListener;
	private ChangeListener<Boolean> calculateOnEndMultiListener;
	
	private final TextFormatter<?> integerFormatter = Util.getIntegerFormatter();
	private final PositiveIntegerTextFormatter positiveIntegerFormatter = new PositiveIntegerTextFormatter();
	
	public IntegerSpecUI(IntegerSpec parent)
	{
		parentSpec = parent;
		
		idLabel = new Label(parentSpec.getId());
		idLabel.getStyleClass().add("spec-label");
		idLabel.prefWidthProperty().bind(widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		

		inputField = new TextField();
		inputField.getStyleClass().add("spec-text-field");
		inputField.prefWidthProperty().bind(widthProperty().multiply(0.6));
		inputField.setMaxWidth(Control.USE_PREF_SIZE);
		
		getChildren().addAll(idLabel, inputField);
		setAlignment(Pos.CENTER_LEFT);
		setMinHeight(Control.USE_PREF_SIZE);

		uniqueBehaviorIdentifierListener = (obs, oldVal, newVal) ->
		{
			if(newVal != null) formatLabel(newVal);
			else formatLabel("");
		};

		holdSetTillDoneListener = (obs, oldVal, newVal) ->
		{
			if(newVal != null)
			{
				if(newVal) setHoldCalculateTillCompleteBindings(HoldType.HOLD);
				else setHoldCalculateTillCompleteBindings(HoldType.DONT_HOLD);
			}
			else setHoldCalculateTillCompleteBindings(HoldType.CLEAR);
		};
		
		positiveOnlyListener = (obs, oldVal, newVal) ->
		{
			if(newVal != null && newVal) inputField.setTextFormatter(positiveIntegerFormatter);
			else inputField.setTextFormatter(integerFormatter);
		};
		
		avoidEmptyListener = (obs, oldVal, newVal) -> { if (!newVal) { if(inputField.getText().equals("")) inputField.setText("0"); } };
		
		calculateOnEndListener = (obs, oldVal, newVal) ->
		{
			if (!newVal)
			{
				if(inputField.getText().equals(""))
				{
					inputField.setText("0");
					parentSpec.setValue(0);
				}
				else parentSpec.setValue(Integer.parseInt(inputField.getText()));
			}
		};
		
		updateFieldOnValueChangeListener = (obs, oldVal, newVal) ->
		{
			if (newVal != null) { inputField.textProperty().setValue(newVal.toString()); }
		};
	}

	@Override
	public Node getUINode() { return this; }

	@Override
	public void unbindUI()
	{
		inputField.disableProperty().unbind();
		parentSpec.getUniqueBehaviorNotifierProperty().removeListener(uniqueBehaviorIdentifierListener);
		parentSpec.getHoldCalculateTillCompleteProperty().removeListener(holdSetTillDoneListener);
		parentSpec.getPositiveOnlyProperty().removeListener(positiveOnlyListener);
		setHoldCalculateTillCompleteBindings(HoldType.CLEAR);
		if(calculateOnEndMultiListener != null) inputField.focusedProperty().removeListener(calculateOnEndMultiListener);
	}

	@Override
	public void rebindUI(String popoutID)
	{
		inputField.disableProperty().bind(parentSpec.getReadOnlyProperty().or(Bindings.createBooleanBinding(() -> parentSpec.getParentBuild().parentIsExpired())));

		if(parentSpec.getUniqueBehaviorNotifierProperty().isNotEmpty().get()) formatLabel(parentSpec.getUniqueBehaviorNotifierProperty().get());
		else formatLabel("");
		parentSpec.getUniqueBehaviorNotifierProperty().addListener(uniqueBehaviorIdentifierListener);
		
		if(parentSpec.isPositiveOnly()) inputField.setTextFormatter(positiveIntegerFormatter);
		else inputField.setTextFormatter(integerFormatter);
		parentSpec.getPositiveOnlyProperty().addListener(positiveOnlyListener);
		
		if(parentSpec.getHoldCalculateTillCompleteProperty().get()) setHoldCalculateTillCompleteBindings(HoldType.HOLD);
		else setHoldCalculateTillCompleteBindings(HoldType.DONT_HOLD);
		parentSpec.getHoldCalculateTillCompleteProperty().addListener(holdSetTillDoneListener);
	}

	// Multiselect should ALWAYS hold till complete to set, in case there is a mix of held and unheld specs.
	@Override
	public void rebindMultiUI(List<Spec<Number>> specs, String popoutID)
	{
		calculateOnEndMultiListener = (obs, oldVal, newVal) ->
		{
			if (!newVal)
			{
				if(inputField.getText().equals("") || inputField.getText().equals("-"))
				{
					inputField.setText("0");
					specs.forEach(s -> s.setValue(0));
				}
				else specs.forEach(s -> s.setValue(Integer.parseInt(inputField.getText())));
			}
		};
		
		inputField.focusedProperty().addListener(calculateOnEndMultiListener);
		// Multiselect integers should ALL run positives only, in case any have that limitation
		inputField.setTextFormatter(positiveIntegerFormatter);
		
		String firstVal = "";
		try
		{
			firstVal = specs.get(0).getValue() + "";
			for(int x = 1 ; x < specs.size() ; x++)
			{
				String nextVal = "";
				try { nextVal = specs.get(x).getValue() + ""; } catch(Exception e) {}
				if(!firstVal.equals(nextVal))
				{
					firstVal = "-";
					break;
				}
			}
		}
		catch (Exception e) {}
		inputField.setText(firstVal);
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
	
	private void setHoldCalculateTillCompleteBindings(HoldType hold)
	{
		if(hold == HoldType.HOLD)
		{
			inputField.focusedProperty().removeListener(avoidEmptyListener);
			
			inputField.textProperty().unbindBidirectional(parentSpec);
			inputField.textProperty().setValue(parentSpec.get().toString());
			
			inputField.focusedProperty().addListener(calculateOnEndListener);
			parentSpec.addListener(updateFieldOnValueChangeListener);
		}
		else if(hold == HoldType.DONT_HOLD)
		{
			inputField.focusedProperty().removeListener(calculateOnEndListener);
			parentSpec.removeListener(updateFieldOnValueChangeListener);
			
			inputField.setText(parentSpec.getValue().intValue() + "");
			inputField.textProperty().bindBidirectional(parentSpec, new NonNullIntegerStringConverter());
			
			inputField.focusedProperty().addListener(avoidEmptyListener);
		}
		else if(hold == HoldType.CLEAR)
		{
			inputField.focusedProperty().removeListener(calculateOnEndListener);
			inputField.focusedProperty().removeListener(avoidEmptyListener);
			parentSpec.removeListener(updateFieldOnValueChangeListener);
			inputField.textProperty().unbindBidirectional(parentSpec);
		}
	}

	private boolean bound = false;
	@Override @JsonIgnore public boolean isBound() { return bound; }
	@Override @JsonIgnore public void setBound() { bound = true; }
}
