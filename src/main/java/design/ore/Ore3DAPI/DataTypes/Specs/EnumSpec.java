package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.Setter;

@JsonSerialize(using = SpecSerialization.EnumSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.EnumSerialization.Deserializer.class)
public class EnumSpec<E extends Enum<E>> extends Spec<E>
{
	public EnumSpec(String id, E initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{
		super(id, new SimpleObjectProperty<E>(initialValue), readOnly, section, countsAsMatch);
		this.clazz = initialValue.getDeclaringClass();
	}
	
	public EnumSpec() { this.property = new SimpleObjectProperty<E>(); }

	@Getter @Setter Class<E> clazz;
	
	public void setValue(Enum<?> value) { if(!readOnly) property.setValue((E) value); }
	
	@Override
	public Pane getUI(List<Spec<?>> toBind, String popoutID)
	{
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
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
		if(readOnly) dropdown.setDisable(true);
		
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
				toBind.forEach(p -> { ((Property<E>)p).setValue(dropdown.getValue()); });
			});
		}
		else
		{
			dropdown.valueProperty().bindBidirectional(property);
		}
		
		HBox input = new HBox(idLabel, dropdown);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		dropdown.prefWidthProperty().bind(input.widthProperty().multiply(0.6));
		dropdown.setMaxWidth(Control.USE_PREF_SIZE);
		
		input.setPrefHeight(25);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
}
