package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.DataTypes.Build.Build;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.Setter;

@JsonSerialize(using = SpecSerialization.EnumSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.EnumSerialization.Deserializer.class)
public class EnumSpec<E extends Enum<E>> extends Spec<E>
{
	public EnumSpec() {}
	
	public EnumSpec(Build parent, String id, E initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public EnumSpec(Build parent, String id, E initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<E> calculateOnDirty)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, null); }
	
	public EnumSpec(Build parent, String id, E initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<E> calculateOnDirty, String uniqueBehaviorNotifier)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }
	
	public EnumSpec(Build parent, String id, E initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, Callable<E> calculateOnDirty, String uniqueBehaviorNotifier)
	{
		super(parent, id, new SimpleObjectProperty<E>(initialValue), readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier);
		this.clazz = initialValue.getDeclaringClass();
	}

	@Getter @Setter Class<E> clazz;
	
	public void setValue(Enum<?> value) { valueProperty.setValue((E) value); }
	
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
		
		ChoiceBox<E> dropdown = new ChoiceBox<>();
		dropdown.getItems().setAll(clazz.getEnumConstants());
		dropdown.setConverter(new StringConverter<E>()
		{
			@Override
			public String toString(E object)
			{
				if(object == null) return "-";
				else return object.toString();
			}

			@Override
			public E fromString(String string)
			{
				for(E en : clazz.getEnumConstants())
				{
					if(en.name().equalsIgnoreCase(string)) return en;
				}
				return null;
			}
		});
		dropdown.disableProperty().bind(readOnlyProperty.or(Bindings.createBooleanBinding(() -> parent.parentIsExpired())));
		
		if(toBind != null && toBind.size() > 0)
		{
			try
			{
				E firstVal = (E) toBind.get(0).getValue();
				for(int x = 1 ; x < toBind.size() ; x++)
				{
					try
					{
						E nextVal = (E) toBind.get(x).getValue();
						if(firstVal != nextVal)
						{
							dropdown.getSelectionModel().clearSelection();
							firstVal = null;
							break;
						}
					} catch(Exception e) { dropdown.getSelectionModel().clearSelection(); firstVal = null; }
				}
				if(firstVal != null) dropdown.setValue(firstVal);
			}
			catch (Exception e) { dropdown.getSelectionModel().clearSelection(); }
			
			dropdown.valueProperty().addListener(l ->
			{
				toBind.forEach(p -> { ((Spec<E>)p).setValue(dropdown.getValue()); });
			});
		}
		else
		{
			dropdown.valueProperty().bindBidirectional(valueProperty);
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
