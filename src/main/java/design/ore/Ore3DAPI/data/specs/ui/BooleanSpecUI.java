package design.ore.Ore3DAPI.data.specs.ui;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.Ore3DAPI.Util.UI;
import design.ore.Ore3DAPI.data.interfaces.ISpecUI;
import design.ore.Ore3DAPI.data.specs.BooleanSpec;
import design.ore.Ore3DAPI.data.specs.Spec;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class BooleanSpecUI extends HBox implements ISpecUI<Boolean>
{
	private final BooleanSpec parentSpec;
	
	private final CheckBox check;
	private final Label idLabel;
	
	private final ChangeListener<String> uniqueBehaviorIdentifierListener;
	private ChangeListener<Boolean> multiselectListener;
	
	public BooleanSpecUI(BooleanSpec parent)
	{
		parentSpec = parent;
		
		idLabel = new Label(parentSpec.getId());
		idLabel.getStyleClass().add("spec-label");
		idLabel.prefWidthProperty().bind(widthProperty().multiply(0.4));
		idLabel.setMaxWidth(USE_PREF_SIZE);
		
		check = new CheckBox();
		check.getStyleClass().add("spec-check-box");
		check.maxHeightProperty().bind(idLabel.heightProperty());
		UI.checkboxMatchSize(check);
		
		getChildren().addAll(idLabel, check);
		setAlignment(Pos.CENTER_LEFT);
		setMinHeight(USE_PREF_SIZE);
		setFillHeight(true);
		
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
		check.disableProperty().unbind();
		parentSpec.getUniqueBehaviorNotifierProperty().removeListener(uniqueBehaviorIdentifierListener);
		check.selectedProperty().unbindBidirectional(parentSpec);
		if(multiselectListener != null) check.selectedProperty().removeListener(multiselectListener);
	}

	// Hold till calculate does nothing with check boxes, so no need to implement
	@Override
	public void rebindUI(String popoutID)
	{
		check.disableProperty().bind(parentSpec.getReadOnlyProperty().or(Bindings.createBooleanBinding(() -> parentSpec.getParentBuild().parentIsExpired())));
		parentSpec.getUniqueBehaviorNotifierProperty().addListener(uniqueBehaviorIdentifierListener);
		check.selectedProperty().bindBidirectional(parentSpec);
	}

	@Override
	public void rebindMultiUI(List<Spec<Boolean>> specs, String popoutID)
	{
		try
		{
			boolean firstVal = specs.get(0).getValue();
			for(int x = 1 ; x < specs.size() ; x++)
			{
				boolean nextVal = specs.get(x).getValue();
				if(firstVal != nextVal)
				{
					check.setIndeterminate(true);
					break;
				}
			}
			check.setSelected(firstVal);
		}
		catch (Exception e) { check.setIndeterminate(true); }

		multiselectListener = (obs, oldVal, newVal) -> specs.forEach(p -> p.setValue(newVal));
		check.selectedProperty().addListener(multiselectListener);
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
