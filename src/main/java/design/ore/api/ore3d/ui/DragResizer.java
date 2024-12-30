package design.ore.api.ore3d.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public class DragResizer
{
	private static final int RESIZE_MARGIN = 10;
	
	private static final double WIDTH_MIN = 50;
	private static final double HEIGHT_MIN = 50;

    private final Region region;
    private boolean initMinHeight;

    private boolean draggingNorth;
    private boolean draggingEast;
    private boolean draggingSouth;
    private boolean draggingWest;

    private final ObjectProperty<Point2D> mouseLocation = new SimpleObjectProperty<>();
    
    private DragResizer(Region aRegion) {
        region = aRegion;
    }

    public static void makeResizable(Region region) {
        final DragResizer resizer = new DragResizer(region);

        region.setOnMousePressed(resizer::mousePressed);
        region.setOnMouseDragged(resizer::mouseDragged);
        region.setOnMouseMoved(resizer::mouseOver);
        region.setOnMouseReleased(resizer::mouseReleased);
    }

    protected void mouseOver(MouseEvent event) {
        if (isInDraggableZoneN(event) || draggingNorth) {
            if(isInDraggableZoneE(event) || draggingEast) {
                region.setCursor(Cursor.NE_RESIZE);
            }
            else if(isInDraggableZoneW(event) || draggingWest) {
                region.setCursor(Cursor.NW_RESIZE);
            }
            else {
                region.setCursor(Cursor.N_RESIZE);
            }
        }
        else if (isInDraggableZoneS(event) || draggingSouth) {
            if(isInDraggableZoneE(event) || draggingEast) {
                region.setCursor(Cursor.SE_RESIZE);
            }
            else if(isInDraggableZoneW(event) || draggingWest) {
                region.setCursor(Cursor.SW_RESIZE);
            }
            else {
                region.setCursor(Cursor.S_RESIZE);
            }
        }
        else if (isInDraggableZoneE(event) || draggingEast) {
            region.setCursor(Cursor.E_RESIZE);
        }
        else if (isInDraggableZoneW(event) || draggingWest) {
            region.setCursor(Cursor.W_RESIZE);
        }
        else {
            region.setCursor(Cursor.DEFAULT);
        }
    }

    private void mousePressed(MouseEvent event) {

        event.consume();

        draggingNorth = isInDraggableZoneN(event);
        draggingEast = isInDraggableZoneE(event);
        draggingSouth = isInDraggableZoneS(event);
        draggingWest = isInDraggableZoneW(event);

        mouseLocation.set(new Point2D((float)event.getScreenX(), (float)event.getScreenY()));

        if (!initMinHeight) {
            region.setPrefHeight(region.getHeight());
            region.setPrefWidth(region.getWidth());
            initMinHeight = true;
        }
    }

    private boolean isInDraggableZoneN(MouseEvent event) {
        return event.getY() < RESIZE_MARGIN;
    }

    private boolean isInDraggableZoneW(MouseEvent event) {
        return event.getX() < RESIZE_MARGIN;
    }

    private boolean isInDraggableZoneS(MouseEvent event) {
        return event.getY() > (region.getHeight() - RESIZE_MARGIN);
    }

    private boolean isInDraggableZoneE(MouseEvent event) {
        return event.getX() > (region.getWidth() - RESIZE_MARGIN);
    }

    private void mouseDragged(MouseEvent event) {

        event.consume();

        if (draggingSouth) resizeSouth(event);
        if (draggingEast) resizeEast(event);
        if (draggingNorth) resizeNorth(event);
        if (draggingWest) resizeWest(event);
    }

    private void resizeEast(MouseEvent event) {
        region.setPrefWidth(event.getX() < WIDTH_MIN ? WIDTH_MIN : event.getX());
    }

    private void resizeSouth(MouseEvent event) {
        region.setPrefHeight(event.getY() < HEIGHT_MIN ? HEIGHT_MIN : event.getY());
    }
    
    private void resizeNorth(MouseEvent event) {
        double prevMin = region.getPrefHeight();
        region.setPrefHeight(region.getPrefHeight() - event.getY());
        if (region.getMinHeight() < region.getPrefHeight()) {
            region.setPrefHeight(region.getPrefHeight());
            region.setTranslateY(region.getTranslateY() - (region.getPrefHeight() - prevMin));
            return;
        }
        if (region.getMinHeight() > region.getPrefHeight() || event.getY() < 0)
            region.setTranslateY(region.getTranslateY() + event.getY());
    }

    private void resizeWest(MouseEvent event) {
        double prevMin = region.getPrefWidth();
        region.setPrefWidth(region.getPrefWidth() - event.getX());
        if (region.getMinWidth() < region.getPrefWidth()) {
            region.setPrefWidth(region.getPrefWidth());
            region.setTranslateX(region.getTranslateX() - (region.getPrefWidth() - prevMin));
            return;
        }
        if (region.getMinWidth() > region.getPrefWidth() || event.getX() < 0)
            region.setTranslateX(region.getTranslateX() + event.getX());
    }

    protected void mouseReleased(MouseEvent event) {
        initMinHeight = false;
        draggingNorth = false; draggingEast = false; draggingSouth = false; draggingWest = false;
        region.setCursor(Cursor.DEFAULT);
        mouseLocation.set(null);
    }

    public static void clearResizable(Region region) {
        region.setOnMousePressed(null);
        region.setOnMouseDragged(null);
        region.setOnMouseMoved(null);
        region.setOnMouseReleased(null);
    }
}