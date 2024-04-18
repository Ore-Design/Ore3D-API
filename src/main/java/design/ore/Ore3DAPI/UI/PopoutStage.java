package design.ore.Ore3DAPI.UI;

import design.ore.Ore3DAPI.Util.Colors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;

public class PopoutStage extends Stage
{
	Scene scene;
	
	@Getter protected final BooleanProperty closeOnTrue;
	
	public PopoutStage(Stage parentStage, Pane content, String title, boolean useStylesheet)
	{
		this.initOwner(parentStage);
		this.setMinWidth(600);
		this.setMinHeight(400);
		this.setTitle(title);
		
		scene = new Scene(content);
		if(useStylesheet) scene.getStylesheets().add("stylesheets/dark.css");
		scene.setFill(Colors.getBackground());
		this.setScene(scene);
		
//		content.prefWidthProperty().bind(scene.widthProperty());
//		content.prefHeightProperty().bind(scene.heightProperty());
		content.setMinSize(600, 400);
		content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		closeOnTrue = new SimpleBooleanProperty(false);
		closeOnTrue.addListener((obs, oldVal, newVal) -> { if(newVal && this.isShowing()) this.close(); });
		setOnCloseRequest(e -> { if(!closeOnTrue.get()) closeOnTrue.setValue(true); });
	}
}
