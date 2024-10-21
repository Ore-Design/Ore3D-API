package design.ore.Ore3DAPI.data.specs.ui;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.data.interfaces.ISpecUI;
import design.ore.Ore3DAPI.data.specs.Spec;
import design.ore.Ore3DAPI.data.specs.StringSpec;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class StringSpecUI extends HBox implements ISpecUI<String>
{
	private final StringSpec parentSpec;
	
	private final TextField inputField;
	private final Label idLabel;
	
	private final ChangeListener<String> uniqueBehaviorIdentifierListener;
	private final ChangeListener<Boolean> holdSetTillDoneListener;
	private final ChangeListener<String> updateFieldOnValueChangeListener;
	private final ChangeListener<Boolean> calculateOnEndListener;
	private ChangeListener<Boolean> calculateOnEndMultiListener;
	
	public StringSpecUI(StringSpec parent)
	{
		parentSpec = parent;
		
		idLabel = new Label(parentSpec.getId());
		idLabel.getStyleClass().add("spec-label");
		idLabel.prefWidthProperty().bind(widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		

		inputField = new TextField();
		inputField.getStyleClass().add("spec-text-field");
		inputField.setTextFormatter(Util.getDecimalFormatter(2));
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
		
		calculateOnEndListener = (obs, oldVal, newVal) ->
		{
			if (!newVal)
			{ parentSpec.setValue(inputField.getText()); }
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
		
		updateFieldOnValueChangeListener = (obs, oldVal, newVal) -> { if (newVal != null) inputField.textProperty().setValue(newVal); };
	}

	@Override
	public Node getUINode() { return this; }

	@Override
	public void unbindUI()
	{
		inputField.disableProperty().unbind();
		parentSpec.getUniqueBehaviorNotifierProperty().removeListener(uniqueBehaviorIdentifierListener);
		parentSpec.getHoldCalculateTillCompleteProperty().removeListener(holdSetTillDoneListener);
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
		
		if(parentSpec.getHoldCalculateTillCompleteProperty().get()) setHoldCalculateTillCompleteBindings(HoldType.HOLD);
		else setHoldCalculateTillCompleteBindings(HoldType.DONT_HOLD);
		parentSpec.getHoldCalculateTillCompleteProperty().addListener(holdSetTillDoneListener);
	}

	// Multiselect should ALWAYS hold till complete to set, in case there is a mix of held and unheld specs.
	@Override
	public void rebindMultiUI(List<Spec<String>> specs, String popoutID)
	{
		calculateOnEndMultiListener = (obs, oldVal, newVal) ->
		{
			if (!newVal) { specs.forEach(s -> s.setValue(inputField.getText())); }
		};
		
		inputField.focusedProperty().addListener(calculateOnEndMultiListener);
		
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
			inputField.textProperty().unbindBidirectional(parentSpec);
			inputField.textProperty().setValue(parentSpec.get());
			
			parentSpec.addListener(updateFieldOnValueChangeListener);
			inputField.focusedProperty().addListener(calculateOnEndListener);
		}
		else if(hold == HoldType.DONT_HOLD)
		{
			inputField.focusedProperty().removeListener(calculateOnEndListener);
			parentSpec.removeListener(updateFieldOnValueChangeListener);
			
			inputField.textProperty().bindBidirectional(parentSpec);
		}
		else if(hold == HoldType.CLEAR)
		{
			inputField.focusedProperty().removeListener(calculateOnEndListener);
			parentSpec.removeListener(updateFieldOnValueChangeListener);
			inputField.textProperty().unbindBidirectional(parentSpec);
		}
	}

	private boolean bound = false;
	@Override @JsonIgnore public boolean isBound() { return bound; }
	@Override @JsonIgnore public void setBound() { bound = true; }
}
