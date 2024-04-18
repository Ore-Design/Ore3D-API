package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.Util.Colors;
import design.ore.Ore3DAPI.Util.Log;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@JsonSerialize(using = SpecSerialization.LargeTextSerialization.Serializer.class)
@JsonDeserialize(using = SpecSerialization.LargeTextSerialization.Deserializer.class)
public class LargeTextSpec extends Spec<String>
{
	public LargeTextSpec(Build parent, String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public LargeTextSpec(Build parent, String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<String> calculateOnDirty, String uniqueBehaviorNotifier)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }
	
	public LargeTextSpec(Build parent, String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<String> calculateOnDirty)
	{ this(parent, id, initialValue, Bindings.createBooleanBinding(() -> readOnly), section, countsAsMatch, calculateOnDirty, null); }
	
	public LargeTextSpec(Build parent, String id, String initialValue, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, Callable<String> calculateOnDirty, String uniqueBehaviorNotifier)
	{ super(parent, id, new SimpleStringProperty(initialValue), readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }

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
		
		Button openPopoutButton = new Button("Edit");
		openPopoutButton.getStyleClass().add("spec-button");
		openPopoutButton.disableProperty().bind(readOnlyProperty.or(Bindings.createBooleanBinding(() -> parent.parentIsExpired())));
		
		HBox input = new HBox(idLabel, openPopoutButton);
		input.setAlignment(Pos.CENTER_LEFT);

		idLabel.prefWidthProperty().bind(input.widthProperty().multiply(0.6));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		openPopoutButton.prefWidthProperty().bind(input.widthProperty().multiply(0.4));
		openPopoutButton.setMaxWidth(Control.USE_PREF_SIZE);
		String title = parent.getParentTransactionProperty().isNull().get() ? id : id + " - " + parent.getParentTransactionProperty().get().getDisplayName();
		openPopoutButton.setOnAction(e -> Util.UI.showPopup(createPopoutUI(toBind), popoutID, title, true));
		
		input.setPrefHeight(20);
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
				toBind.forEach(p ->
				{
					if(p instanceof LargeTextSpec) ((LargeTextSpec) p).setValue(editArea.getText());
					else Log.getLogger().warn("Non-LargeTextSpec passed into LargeTextSpec multiselect!");
				});
			});
		}
		else editArea.textProperty().bindBidirectional(valueProperty);

		VBox layout = new VBox(title, editArea);
		layout.setFillWidth(true);
		layout.setPadding(new Insets(10));
		layout.setSpacing(10);
		layout.setBackground(Background.fill(Colors.getBackground()));
		
		return layout;
	}
}
