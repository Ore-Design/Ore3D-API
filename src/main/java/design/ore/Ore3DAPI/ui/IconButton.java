package design.ore.Ore3DAPI.ui;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class IconButton extends Button
{
	public IconButton(ImageView icon, boolean bindHeight)
	{
		super();

		icon.setPreserveRatio(true);
		
		if(bindHeight)
		{
			prefHeightProperty().bind(widthProperty());
			icon.fitHeightProperty().bind(widthProperty().multiply(0.65));
		}
		else
		{
			prefWidthProperty().bind(heightProperty());
			icon.fitWidthProperty().bind(heightProperty().multiply(0.65));
		}
		
		setMinSize(0, 0);
		setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		setGraphic(icon);
		getStyleClass().add("icon-button");
	}
}
