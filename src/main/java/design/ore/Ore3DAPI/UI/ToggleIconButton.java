package design.ore.Ore3DAPI.UI;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class ToggleIconButton extends Button
{
	final SimpleBooleanProperty toggledProperty = new SimpleBooleanProperty();
	
	public ToggleIconButton(ImageView iconToggledTrue, ImageView iconToggledFalse, boolean bindHeight)
	{
		super();
		
		setSizeBindings(iconToggledTrue, bindHeight);
		setSizeBindings(iconToggledFalse, bindHeight);
		
		if(bindHeight) this.prefHeightProperty().bind(this.widthProperty());
		else this.prefWidthProperty().bind(this.heightProperty());
		this.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		this.setMinSize(10, 10);
		
		this.setGraphic(toggledProperty.get() ? iconToggledTrue : iconToggledFalse);
		this.getStyleClass().add("icon-button");
		this.setOnAction(e -> toggledProperty.setValue(!toggledProperty.getValue()));
		
		toggledProperty.addListener((obs, oldVal, newVal) -> this.setGraphic(newVal ? iconToggledTrue : iconToggledFalse));
	}
	
	public void bind(BooleanProperty binding) { toggledProperty.bindBidirectional(binding); }
	public void unbind(BooleanProperty binding) { toggledProperty.unbindBidirectional(binding); }
	
	private void setSizeBindings(ImageView icon, boolean bindHeight)
	{
		icon.setPreserveRatio(true);
		this.heightProperty().addListener(l ->
		{
			if(this.getHeight() >= this.getWidth())
			{
				icon.setFitWidth(this.getWidth() * 0.7);
				icon.setFitHeight(icon.getFitWidth());
			}
			else
			{
				icon.setFitHeight(this.getHeight() * 0.7);
				icon.setFitWidth(icon.getFitHeight());
			}
		});
		
		this.widthProperty().addListener(l ->
		{
			if(this.getHeight() >= this.getWidth())
			{
				icon.setFitWidth(this.getWidth() * 0.7);
				icon.setFitHeight(icon.getFitWidth());
			}
			else
			{
				icon.setFitHeight(this.getHeight() * 0.7);
				icon.setFitWidth(icon.getFitHeight());
			}
		});
	}
}
