package design.ore.Ore3DAPI.UI;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class IconButton extends Button
{
	public IconButton(ImageView icon, boolean bindHeight)
	{
		super();

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
		
		if(bindHeight) this.prefHeightProperty().bind(this.widthProperty());
		else this.prefWidthProperty().bind(this.heightProperty());
		this.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		this.setMinSize(10, 10);
		
		this.setGraphic(icon);
		this.getStyleClass().add("icon-button");
	}
}
