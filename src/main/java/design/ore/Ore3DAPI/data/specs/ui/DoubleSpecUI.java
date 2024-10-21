package design.ore.Ore3DAPI.data.specs.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Colors;
import design.ore.Ore3DAPI.data.interfaces.ISpecUI;
import design.ore.Ore3DAPI.data.specs.DoubleSpec;
import design.ore.Ore3DAPI.data.specs.Spec;
import design.ore.Ore3DAPI.javafx.NonNullDoubleStringConverter;
import design.ore.Ore3DAPI.ui.ToggleIconButton;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class DoubleSpecUI extends HBox implements ISpecUI<Number>
{
	private final DoubleSpec parentSpec;
	
	private final TextField inputField;
	private final Label idLabel;
	private final ToggleIconButton linkToggleButton;
	
	private final ChangeListener<String> uniqueBehaviorIdentifierListener;
	private final ChangeListener<Boolean> holdSetTillDoneListener;
	private final ChangeListener<Boolean> avoidEmptyListener;
	private final ChangeListener<Boolean> calculateOnEndListener;
	private final ChangeListener<Number> updateFieldOnValueChangeListener;
	private ChangeListener<Boolean> calculateOnEndMultiListener;
	
	public DoubleSpecUI(DoubleSpec parent)
	{
		parentSpec = parent;
		
		idLabel = new Label(parentSpec.getId());
		idLabel.getStyleClass().add("spec-label");
		idLabel.prefWidthProperty().bind(widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);

		linkToggleButton = new ToggleIconButton(
			Util.UI.colorize(new ImageView(Util.getChainIcon()), Colors.getAccentProperty()),
			Util.UI.colorize(new ImageView(Util.getBrokenChainIcon()), Colors.getAccentProperty()),
			false);
		linkToggleButton.setOnAction(e -> parentSpec.setLinkIsActive(!parentSpec.linkIsActive()));
		linkToggleButton.prefHeightProperty().bind(heightProperty());
		linkToggleButton.setMaxWidth(USE_PREF_SIZE);
		linkToggleButton.maxHeightProperty().bind(linkToggleButton.widthProperty());
		linkToggleButton.setMinHeight(0);

		inputField = new TextField();
		inputField.getStyleClass().add("spec-text-field");
		inputField.setTextFormatter(Util.getDecimalFormatter(2));
		inputField.prefWidthProperty().bind(widthProperty().multiply(0.5));
		inputField.setMaxWidth(Control.USE_PREF_SIZE);
		
		getChildren().addAll(idLabel, linkToggleButton, inputField);
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
		
		avoidEmptyListener = (obs, oldVal, newVal) -> { if (!newVal) { if(inputField.getText().equals("")) inputField.setText("0.0"); } };
		
		calculateOnEndListener = (obs, oldVal, newVal) ->
		{
			if (!newVal)
			{
				if(inputField.getText().equals(""))
				{
					inputField.setText("0.0");
					parentSpec.setValue(0.0);
				}
				else parentSpec.setValue(Double.parseDouble(inputField.getText()));
			}
		};
		
		updateFieldOnValueChangeListener = (obs, oldVal, newVal) ->
		{
			if (newVal != null && newVal.doubleValue() != Double.NaN && !Double.isInfinite(newVal.doubleValue()))
			{
				inputField.textProperty().setValue(new BigDecimal(newVal.doubleValue()).setScale(2, RoundingMode.HALF_UP) + "");
			}
		};
	}

	@Override
	public Node getUINode() { return this; }

	@Override
	public void unbindUI()
	{
		inputField.disableProperty().unbind();
		linkToggleButton.visibleProperty().unbind();
		linkToggleButton.unbind(parentSpec.getLinkIsActiveProperty());
		parentSpec.getUniqueBehaviorNotifierProperty().removeListener(uniqueBehaviorIdentifierListener);
		parentSpec.getHoldCalculateTillCompleteProperty().removeListener(holdSetTillDoneListener);
		setHoldCalculateTillCompleteBindings(HoldType.CLEAR);
		if(calculateOnEndMultiListener != null) inputField.focusedProperty().removeListener(calculateOnEndMultiListener);
	}

	@Override
	public void rebindUI(String popoutID)
	{
		inputField.disableProperty().bind(parentSpec.getReadOnlyProperty().or(Bindings.createBooleanBinding(() -> parentSpec.getParentBuild().parentIsExpired())));
		linkToggleButton.visibleProperty().bind(parentSpec.isLinked());
		linkToggleButton.bind(parentSpec.getLinkIsActiveProperty());

		if(parentSpec.getUniqueBehaviorNotifierProperty().isNotEmpty().get()) formatLabel(parentSpec.getUniqueBehaviorNotifierProperty().get());
		else formatLabel("");
		parentSpec.getUniqueBehaviorNotifierProperty().addListener(uniqueBehaviorIdentifierListener);
		
		if(parentSpec.getHoldCalculateTillCompleteProperty().get()) setHoldCalculateTillCompleteBindings(HoldType.HOLD);
		else setHoldCalculateTillCompleteBindings(HoldType.DONT_HOLD);
		parentSpec.getHoldCalculateTillCompleteProperty().addListener(holdSetTillDoneListener);
	}

	// Multiselect should ALWAYS hold till complete to set, in case there is a mix of held and unheld specs.
	@Override
	public void rebindMultiUI(List<Spec<Number>> specs, String popoutID)
	{
		inputField.disableProperty().bind(parentSpec.getReadOnlyProperty().or(Bindings.createBooleanBinding(() -> parentSpec.getParentBuild().parentIsExpired())));
		calculateOnEndMultiListener = (obs, oldVal, newVal) ->
		{
			if (!newVal)
			{
				if(inputField.getText().equals("") || inputField.getText().equals("-"))
				{
					inputField.setText("0.0");
					specs.forEach(s -> s.setValue(0.0));
				}
				else specs.forEach(s -> s.setValue(Double.parseDouble(inputField.getText())));
			}
		};

		linkToggleButton.bind(parentSpec.getLinkIsActiveProperty());
		linkToggleButton.setVisible(specs.stream().allMatch(s -> s.isLinked().get()));
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
			inputField.focusedProperty().removeListener(avoidEmptyListener);
			
			inputField.textProperty().unbindBidirectional(parentSpec);
			inputField.textProperty().setValue(new BigDecimal(parentSpec.get().doubleValue()).setScale(2, RoundingMode.HALF_UP) + "");
			
			inputField.focusedProperty().addListener(calculateOnEndListener);
			parentSpec.addListener(updateFieldOnValueChangeListener);
		}
		else if(hold == HoldType.DONT_HOLD)
		{
			inputField.focusedProperty().removeListener(calculateOnEndListener);
			parentSpec.removeListener(updateFieldOnValueChangeListener);
			
			inputField.textProperty().bindBidirectional(parentSpec, new NonNullDoubleStringConverter());
			
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
