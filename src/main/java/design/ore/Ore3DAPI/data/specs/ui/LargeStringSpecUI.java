package design.ore.Ore3DAPI.data.specs.ui;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Colors;
import design.ore.Ore3DAPI.data.interfaces.ISpecUI;
import design.ore.Ore3DAPI.data.specs.Spec;
import design.ore.Ore3DAPI.data.specs.StringSpec;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class LargeStringSpecUI extends HBox implements ISpecUI<String>
{
	private final StringSpec parentSpec;
	
	private final Button popoutButton;
	private final Label idLabel;
	
	private final ChangeListener<String> uniqueBehaviorIdentifierListener;
	
	public LargeStringSpecUI(StringSpec parent)
	{
		parentSpec = parent;
		
		idLabel = new Label(parentSpec.getId());
		idLabel.getStyleClass().add("spec-label");
		idLabel.prefWidthProperty().bind(widthProperty().multiply(0.4));
		idLabel.setMaxWidth(Control.USE_PREF_SIZE);
		
		popoutButton = new Button("Edit");
		popoutButton.getStyleClass().add("spec-button");
		popoutButton.prefWidthProperty().bind(widthProperty().multiply(0.6));
		popoutButton.setMaxWidth(Control.USE_PREF_SIZE);
		
		getChildren().addAll(idLabel, popoutButton);
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
		parentSpec.getUniqueBehaviorNotifierProperty().removeListener(uniqueBehaviorIdentifierListener);
		popoutButton.setOnAction(null);
	}

	@Override
	public void rebindUI(String popoutID)
	{
		if(parentSpec.getUniqueBehaviorNotifierProperty().isNotEmpty().get()) formatLabel(parentSpec.getUniqueBehaviorNotifierProperty().get());
		else formatLabel("");
		parentSpec.getUniqueBehaviorNotifierProperty().addListener(uniqueBehaviorIdentifierListener);
		
		String title = parentSpec.getParentBuild().getParentTransactionProperty().isNull().get() ?
			parentSpec.getId() : parentSpec.getId() + " - " + parentSpec.getParentBuild().getParentTransactionProperty().get().getDisplayName();
		popoutButton.setOnAction(e -> Util.UI.showPopup(createPopoutUI(), popoutID, title, true));
	}

	// Multiselect shouldn't allow multiselect at all
	@Override
	public void rebindMultiUI(List<Spec<String>> specs, String popoutID)
	{
		popoutButton.setDisable(true);
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
	
	private Pane createPopoutUI()
	{
		Label title = new Label("Edit " + parentSpec.getId());
		title.getStyleClass().add("small-label");
		title.setMaxWidth(Double.MAX_VALUE);
		
		TextArea editArea = new TextArea();
		editArea.setWrapText(true);
		editArea.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		editArea.textProperty().bindBidirectional(parentSpec);

		VBox layout = new VBox(title, editArea);
		layout.setFillWidth(true);
		layout.setPadding(new Insets(10));
		layout.setSpacing(10);
		layout.backgroundProperty().bind(Bindings.createObjectBinding(() -> Background.fill(Colors.getBackgroundProperty().getValue()), Colors.getBackgroundProperty()));
		
		return layout;
	}

	private boolean bound = false;
	@Override @JsonIgnore public boolean isBound() { return bound; }
	@Override @JsonIgnore public void setBound() { bound = true; }
}
