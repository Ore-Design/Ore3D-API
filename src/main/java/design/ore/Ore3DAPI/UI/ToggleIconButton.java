package design.ore.Ore3DAPI.UI;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class ToggleIconButton extends Button
{
	public ToggleIconButton(ImageView iconToggledTrue, ImageView iconToggledFalse, boolean bindHeight, BooleanProperty binding)
	{
		super();
		
		setSizeBindings(iconToggledTrue, bindHeight);
		setSizeBindings(iconToggledFalse, bindHeight);
		
		if(bindHeight) this.prefHeightProperty().bind(this.widthProperty());
		else this.prefWidthProperty().bind(this.heightProperty());
		this.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		this.setMinSize(10, 10);
		
		this.setGraphic(binding.get() ? iconToggledTrue : iconToggledFalse);
		this.getStyleClass().add("icon-button");
		
		binding.addListener((obs, oldVal, newVal) -> this.setGraphic(newVal ? iconToggledTrue : iconToggledFalse));
	}
	
	private void setSizeBindings(ImageView icon, boolean bindHeight)
	{
		icon.setPreserveRatio(true);
		this.heightProperty().addListener(l ->
		{
			if(this.getHeight() >= this.getWidth())
			{
				icon.setFitWidth(this.getWidth() * 0.65);
				icon.setFitHeight(0);
			}
			else
			{
				icon.setFitWidth(0);
				icon.setFitHeight(this.getHeight() * 0.65);
			}
		});
		
		this.widthProperty().addListener(l ->
		{
			if(this.getHeight() >= this.getWidth())
			{
				icon.setFitWidth(this.getWidth() * 0.65);
				icon.setFitHeight(0);
			}
			else
			{
				icon.setFitWidth(0);
				icon.setFitHeight(this.getHeight() * 0.65);
			}
		});
	}
}
