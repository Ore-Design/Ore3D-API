package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Util;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@JsonSerialize(using = SpecSerialization.LargeTextSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.LargeTextSerialization.Deserializer.class)
public class LargeTextSpec extends Spec<String>
{
	public LargeTextSpec(String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch) { super(id, new SimpleStringProperty(initialValue), readOnly, section, countsAsMatch); }
	public LargeTextSpec(String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<String> calculateOnDirty)
	{ super(id, new SimpleStringProperty(initialValue), readOnly, section, countsAsMatch, calculateOnDirty); }

	@Override
	public Pane getUI(List<Spec<?>> toBind, String popoutID)
	{
		Label idLabel = new Label(id);
		idLabel.getStyleClass().add("spec-label");
		
		Button openPopoutButton = new Button("Edit");
		openPopoutButton.setId("build-select-button");
		openPopoutButton.setPadding(new Insets(0));
		
		HBox input = new HBox(idLabel, openPopoutButton);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.6));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		openPopoutButton.prefWidthProperty().bind(input.widthProperty().multiply(0.4));
		openPopoutButton.setMaxWidth(Control.USE_PREF_SIZE);
		openPopoutButton.setOnAction(e -> Util.UI.showPopup(createPopoutUI(toBind), popoutID));
		
		input.setPrefHeight(25);
		input.setMaxHeight(Control.USE_PREF_SIZE);
		
		return input;
	}
	
	private Pane createPopoutUI(List<Spec<?>> toBind)
	{
		Label title = new Label("Edit " + id);
		title.getStyleClass().add("small-label");
		title.setMaxWidth(Double.MAX_VALUE);
		
		TextArea editArea = new TextArea();
		editArea.setWrapText(true);
		editArea.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		if(toBind != null && toBind.size() > 1)
		{
			editArea.setText("-");
			editArea.textProperty().addListener((obs, oldVal, newVal) ->
			{
				toBind.forEach(p -> { try { ((Property<String>)p).setValue(editArea.getText()); } catch(Exception e) {} });
			});
		}
		else editArea.textProperty().bindBidirectional(property);

		VBox layout = new VBox(title, editArea);
		layout.setFillWidth(true);
		layout.setPadding(new Insets(10));
		layout.setSpacing(10);
		
		return layout;
	}
}
