package design.ore.ore3dapi.ui;

import java.util.Collection;
import java.util.HashSet;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.Setter;

public class MultiSearchableDropdown<T> extends TextField
{
	@Getter final ObservableList<T> items;
	final FilteredList<T> filteredItems;
	public void setItems(Collection<T> items) { this.items.setAll(items); }
	
	@Getter final ObservableSet<T> selectedItems;
	
	private final ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> translatePopup();
	private final EventHandler<? super ScrollEvent> scrollEvent = e -> translatePopup();
	
	private final BooleanBinding paneVisibleBinding;
	public boolean paneIsVisible() { return paneVisibleBinding.get(); }
	
	@Getter @Setter private StringConverter<T> converter;
	
	private final ListView<T> searchList;
	
	public MultiSearchableDropdown() { this(FXCollections.observableArrayList()); }
	
	public MultiSearchableDropdown(ObservableList<T> items)
	{
		super();
		
		this.items = items;
		
		filteredItems = this.items.filtered(item -> true);
		
		selectedItems = FXCollections.observableSet(new HashSet<>());
		
		searchList = new ListView<>(filteredItems);
		searchList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		searchList.prefWidthProperty().bind(widthProperty());
		searchList.setMaxWidth(USE_PREF_SIZE);
		searchList.getStyleClass().add("searchable-dropdown-list");
		searchList.setMaxHeight(300);
		
		paneVisibleBinding = focusedProperty().or(searchList.focusedProperty());
		searchList.visibleProperty().bind(paneVisibleBinding);
		
		textProperty().addListener((obs, oldVal, newVal) ->
		{
			if(newVal != null && !newVal.equals("") && paneVisibleBinding.get())
			{
				filteredItems.setPredicate(item ->
				{
					String converted = converter.toString(item);
					return converted != null && newVal != null && converted.toLowerCase().contains(newVal.toLowerCase());
				});
			}
			else filteredItems.setPredicate(item -> true);
		});
		
		setOnKeyPressed(e -> { if(e.getCode() == KeyCode.ESCAPE) getParent().requestFocus(); });
		
		getStyleClass().add("searchable-dropdown");
		boundsInLocalProperty().addListener((obs, oldVal, newVal) -> translatePopup());
		
		sceneProperty().addListener((obs, oldVal, newVal) ->
		{
			if(oldVal != null && oldVal.getRoot() instanceof StackPane)
			{
				StackPane oldRoot = (StackPane) oldVal.getRoot();
				oldRoot.getChildren().remove(searchList);
				oldRoot.widthProperty().removeListener(resizeListener);
				oldRoot.heightProperty().removeListener(resizeListener);
			}
			if(newVal != null && newVal.getRoot() instanceof StackPane)
			{
				StackPane newRoot = (StackPane) newVal.getRoot();
				newRoot.getChildren().add(searchList);
				newRoot.widthProperty().addListener(resizeListener);
				newRoot.heightProperty().addListener(resizeListener);
				
				translatePopup();
			}
		});
		
		paneVisibleBinding.addListener((obs, oldVal, newVal) ->
		{
			if(newVal == null || !newVal)
			{
				handleTextFill();
				if(getScene() != null) getScene().removeEventFilter(ScrollEvent.ANY, scrollEvent);
			}
			else if(newVal)
			{
				if(getScene() != null) getScene().addEventFilter(ScrollEvent.ANY, scrollEvent);
				setText("");
				translatePopup();
			}
		});
	}
	
	public void handleTextFill()
	{
		if(selectedItems.size() == 0) setText("-");
		else if(selectedItems.size() == 1)
		{
			@SuppressWarnings("unchecked")
			T first = (T) selectedItems.toArray()[0];
			
			if(converter != null) setText(converter.toString(first));
			else setText(first.toString());
		}
		else setText(selectedItems.size() + " values...");
	}
	
	public void setCellFactory(Callback<ListView<T>, ListCell<T>> callback)
	{
		searchList.setCellFactory(listView ->
		{
			ListCell<T> cell = callback.call(listView);
			cell.setMaxWidth(USE_PREF_SIZE);
			cell.setOnMousePressed(e -> { if(!selectedItems.remove(cell.getItem())) selectedItems.add(cell.getItem()); });
			return cell;
		});
	}
	
	private void translatePopup()
	{
		Bounds posInScene = localToScene(getBoundsInLocal());
		
		if(getScene() == null)
		{
			searchList.setTranslateX(posInScene.getMinX());
			searchList.setTranslateY(posInScene.getMaxY());
		}
		else
		{
			double heightPoint = getScene().getHeight() - 350.0;
	
			if(posInScene.getMaxY() > heightPoint)
			{
				searchList.setTranslateX(posInScene.getMinX());
				searchList.setTranslateY(posInScene.getMinY() - searchList.getHeight());
			}
			else
			{
				searchList.setTranslateX(posInScene.getMinX());
				searchList.setTranslateY(posInScene.getMaxY());
			}
		}
	}
}
