package design.ore.Ore3DAPI.UI;

import design.ore.Ore3DAPI.Util;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class PopoutPane extends VBox
{
	Pane innerPane;
	
	HBox dockBar;
	IconButton closeButton;
	
	private final PopoutPane INSTANCE;
	protected double mouseAnchorX;
	protected double mouseAnchorY;
	protected double translateAnchorX;
	protected double translateAnchorY;

	private final EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent event)
		{
			if(event.getButton() == MouseButton.PRIMARY && INSTANCE.getCursor() == Cursor.DEFAULT)
			{
				mouseAnchorX = event.getSceneX();
				mouseAnchorY = event.getSceneY();

				translateAnchorX = INSTANCE.getTranslateX();
				translateAnchorY = INSTANCE.getTranslateY();

				event.consume();
			}
		}
	};

	private final EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event)
		{
			if(event.getButton() == MouseButton.PRIMARY && INSTANCE.getCursor() == Cursor.DEFAULT)
			{
				INSTANCE.setTranslateX(translateAnchorX + event.getSceneX() - mouseAnchorX);
				INSTANCE.setTranslateY(translateAnchorY + event.getSceneY() - mouseAnchorY);

				event.consume();
			}
		}
	};
	
	public PopoutPane(Pane content, Pane parent, IconButton overrideIconButton)
	{
		this.INSTANCE = this;
		
		innerPane = new Pane(content);
//		innerPane.setPadding(new Insets(50, 0, 0, 0));
		innerPane.prefHeightProperty().bind(this.heightProperty());
		innerPane.prefWidthProperty().bind(this.widthProperty());
		
		content.prefWidthProperty().bind(innerPane.widthProperty());
		content.prefHeightProperty().bind(innerPane.heightProperty());
		
		if(overrideIconButton != null)
		{
			closeButton = new IconButton((ImageView) overrideIconButton.getGraphic(), false);
			closeButton.setOnAction(overrideIconButton.getOnAction());
		}
		else
		{
			closeButton = new IconButton(Util.UI.colorize(new ImageView(Util.getXIcon()), Util.Colors.getAccent()), false);
			closeButton.setOnAction(e -> { this.setVisible(false); parent.getChildren().remove(this); });
		}
		
		dockBar = new HBox(closeButton);
		dockBar.setBackground(Background.fill(Util.Colors.getBackground()));
		dockBar.prefWidthProperty().bind(this.widthProperty());
		dockBar.setPrefHeight(20);
		dockBar.setMinHeight(USE_PREF_SIZE);
		dockBar.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		dockBar.setAlignment(Pos.CENTER_RIGHT);
		dockBar.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
		dockBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);

		this.setMinSize(50, 50);
		this.setPrefSize(600, 400);
		this.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
		this.getChildren().addAll(dockBar, innerPane);
		this.getStyleClass().add("popout");
		this.setAlignment(Pos.TOP_CENTER);
		
		this.setTranslateX((parent.getWidth() / 2) - (this.getWidth() / 2));
		this.setTranslateX((parent.getHeight() / 2) - (this.getHeight() / 2));
		
		DragResizer.makeResizable(this);
	}
}
