package design.ore.Ore3DAPI.data.specs.ui;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.Ore3DAPI.data.interfaces.ISpecUI;
import design.ore.Ore3DAPI.data.specs.EnumSpec;
import design.ore.Ore3DAPI.data.specs.Spec;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

public class EnumSpecUI<T extends Enum<T>> extends HBox implements ISpecUI<T>
{
	private final EnumSpec<T> parentSpec;
	
	private final ChoiceBox<T> dropdown;
	private final Label idLabel;
	
	protected final ChangeListener<String> uniqueBehaviorIdentifierListener;
	protected ChangeListener<T> multiChangeListener;
	
	public EnumSpecUI(EnumSpec<T> parent)
	{
		parentSpec = parent;
		
		idLabel = new Label(parentSpec.getId());
		idLabel.getStyleClass().add("spec-label");
		idLabel.prefWidthProperty().bind(widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		
		dropdown = new ChoiceBox<>();
		dropdown.getItems().setAll(parentSpec.getClazz().getEnumConstants());
		
		// This converter makes the multiselect appear as dash, and converts from integer value to string display
		dropdown.setConverter(new StringConverter<T>()
		{
			@Override
			public String toString(T object)
			{
				if(object == null) return "-";
				else return object.toString();
			}

			@Override
			public T fromString(String string)
			{
				return Enum.valueOf(parentSpec.getClazz(), string);
			}
		});
		dropdown.prefWidthProperty().bind(widthProperty().multiply(0.6));
		dropdown.setMaxWidth(Control.USE_PREF_SIZE);
		
		getChildren().addAll(idLabel, dropdown);
		setAlignment(Pos.CENTER_LEFT);
		setMinHeight(Control.USE_PREF_SIZE);

		uniqueBehaviorIdentifierListener = (obs, oldVal, newVal) ->
		{
			if(newVal != null) formatLabel(newVal);
			else formatLabel("");
		};
	}

	@Override
	public Node getUINode() { return this; }

	@Override
	public void unbindUI()
	{
		dropdown.disableProperty().unbind();
		parentSpec.getUniqueBehaviorNotifierProperty().removeListener(uniqueBehaviorIdentifierListener);
		if(multiChangeListener != null) dropdown.valueProperty().removeListener(multiChangeListener);
	}

	@Override
	public void rebindUI(String popoutID)
	{
		dropdown.disableProperty().bind(parentSpec.getReadOnlyProperty().or(Bindings.createBooleanBinding(() -> parentSpec.getParentBuild().parentIsExpired())));
		dropdown.valueProperty().bindBidirectional(parentSpec);

		if(parentSpec.getUniqueBehaviorNotifierProperty().isNotEmpty().get()) formatLabel(parentSpec.getUniqueBehaviorNotifierProperty().get());
		else formatLabel("");
		parentSpec.getUniqueBehaviorNotifierProperty().addListener(uniqueBehaviorIdentifierListener);
	}
	
	@Override
	public void rebindMultiUI(List<Spec<T>> specs, String popoutID)
	{
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
