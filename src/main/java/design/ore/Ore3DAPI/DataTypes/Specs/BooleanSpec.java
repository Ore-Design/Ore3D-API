package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Util;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

@JsonSerialize(using = SpecSerialization.BooleanSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.BooleanSerialization.Deserializer.class)
public class BooleanSpec extends Spec<Boolean>
{
	public BooleanSpec(String id, boolean initialValue, boolean readOnly, String section) { super(id, new SimpleBooleanProperty(initialValue).asObject(), readOnly, section); }

	@Override
	public Pane getUI(List<Spec<?>> toBind)
	{
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
		CheckBox check = new CheckBox();
		if(readOnly) check.setDisable(true);
		
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
			check.selectedProperty().bindBidirectional(property);
		}
		
		HBox input = new HBox(idLabel, check);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		check.setMaxWidth(Double.MAX_VALUE);
		Util.UI.checkboxMatchSize(check);
		HBox.setHgrow(check, Priority.ALWAYS);
		
		input.setPrefHeight(25);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
}
