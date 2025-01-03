package design.ore.ore3dapi.ui;

import design.ore.ore3dapi.Util.Colors;
import design.ore.ore3dapi.Util.Styling;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class PopoutStage<T> extends Stage
{
	Scene scene;
	@Setter private T result;
	
	@Getter protected final BooleanProperty closeOnTrue;
	
	public PopoutStage(Stage parentStage, Pane content, String title, boolean useStylesheet)
	{
		this.initOwner(parentStage);
		this.setMinWidth(600);
		this.setMinHeight(400);
		this.setTitle(title);
		
		scene = new Scene(content);
		if(useStylesheet) Styling.bindUIToStylesheet(scene);
		scene.fillProperty().bind(Colors.getBackgroundProperty());
		this.setScene(scene);
		
//		content.prefWidthProperty().bind(scene.widthProperty());
//		content.prefHeightProperty().bind(scene.heightProperty());
		content.setMinSize(600, 400);
		content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		closeOnTrue = new SimpleBooleanProperty(false);
		closeOnTrue.addListener((obs, oldVal, newVal) -> { if(newVal && this.isShowing()) this.close(); });
		setOnCloseRequest(e -> { if(!closeOnTrue.get()) closeOnTrue.setValue(true); });
	}
	
	public T showAndWaitForResult()
	{
		this.showAndWait();
		return result;
	}
}
