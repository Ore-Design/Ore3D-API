package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;

import design.ore.Ore3DAPI.JavaFX.IntegerTextFormatter;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.converter.IntegerStringConverter;

public class IntSpec extends Spec<Integer>
{
	public IntSpec(String id, int initialValue, boolean readOnly, String section) { super(id, new SimpleIntegerProperty(initialValue).asObject(), readOnly, section); }
	
	private String preEdit = "";

	@Override
	public Pane getUI(List<Property<?>> toBind)
	{
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
		TextField inputField = new TextField();
		inputField.getStyleClass().add("spec-text-field");
		if(readOnly) inputField.setDisable(true);
		
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
					int val = text.equals("") ? 0 : Integer.parseInt(inputField.getText());
					toBind.forEach(p -> { ((Property<Integer>)p).setValue(val); });
				}
				catch(Exception e) { inputField.setText(preEdit); } });
		}
		else
		{
			inputField.setTextFormatter(new IntegerTextFormatter());
			inputField.textProperty().bindBidirectional(this.property, new IntegerStringConverter());
			inputField.focusedProperty().addListener(new ChangeListener<Boolean>()
			{
			    @Override
			    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
			    {
			        if (!newPropertyValue) { if(inputField.getText().equals("")) inputField.setText("0"); }
			    }
			});
		}
		
		HBox input = new HBox(idLabel, inputField);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.5));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		inputField.prefWidthProperty().bind(input.widthProperty().multiply(0.5));
		inputField.setMaxWidth(Control.USE_PREF_SIZE);
		
		input.setPrefHeight(25);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
}