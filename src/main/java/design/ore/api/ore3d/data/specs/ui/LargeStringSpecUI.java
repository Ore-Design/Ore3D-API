package design.ore.api.ore3d.data.specs.ui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import design.ore.api.ore3d.Util;
import design.ore.api.ore3d.Util.Colors;
import design.ore.api.ore3d.data.interfaces.ISpecUI;
import design.ore.api.ore3d.data.specs.Spec;
import design.ore.api.ore3d.data.specs.StringSpec;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

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
		
		TextArea editArea = new TextArea(parentSpec.getValue() == null ? "" : parentSpec.getValue());
		editArea.setWrapText(true);
		editArea.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

		// Commit to the spec on focus lost (or when this popout closes) rather than live-binding every keystroke -
		// a live bidirectional binding here fires a full spec-change -> refresh() cascade (recalculating pricing
		// and description for the whole build tree) on every character typed.
		Runnable commitEdit = () -> { if(!editArea.getText().equals(parentSpec.getValue())) parentSpec.setValue(editArea.getText()); };

		editArea.focusedProperty().addListener((obs, wasFocused, isFocused) -> { if(!isFocused) commitEdit.run(); });

		ChangeListener<String> externalChangeListener = (obs, oldVal, newVal) ->
		{
			if(!editArea.isFocused() && newVal != null && !newVal.equals(editArea.getText())) editArea.setText(newVal);
		};
		parentSpec.addListener(externalChangeListener);

		// The popout's content is removed from the scene graph when its stage closes - use that as the cue to
		// flush any pending edit and unregister the listener above (otherwise it leaks a new one on every open).
		editArea.sceneProperty().addListener((obs, oldScene, newScene) ->
		{
			if(newScene == null)
			{
				commitEdit.run();
				parentSpec.removeListener(externalChangeListener);
			}
		});

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
