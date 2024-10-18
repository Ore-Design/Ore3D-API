package design.ore.Ore3DAPI.ui;

import java.util.Collection;
import java.util.List;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
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
	
	@Getter private final ObservableList<T> selectedValuesProperty = FXCollections.<T>observableArrayList();
	
	public void clearSelection() { searchList.getSelectionModel().clearSelection(); }
	
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
		
		searchList = new ListView<>(filteredItems);
		searchList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		searchList.prefWidthProperty().bind(widthProperty());
		searchList.setMaxWidth(USE_PREF_SIZE);
		searchList.getStyleClass().add("searchable-dropdown-list");
		searchList.setMaxHeight(300);
		
		paneVisibleBinding = focusedProperty().or(searchList.focusedProperty());
		searchList.visibleProperty().bind(paneVisibleBinding);
		
		selectedValuesProperty.addListener((Change<? extends T> c) ->
		{
			while(c.next())
			{
				if(paneVisibleBinding.not().get()) handleTextFill(c.getList());
			}
		});
		
		textProperty().addListener((obs, oldVal, newVal) ->
		{
			if(newVal != null && !newVal.equals(""))
			{
				this.filteredItems.setPredicate(item ->
				{
					String converted = converter.toString(item);
					return converted != null && newVal != null && converted.toLowerCase().contains(newVal.toLowerCase());
				});
			}
			else this.filteredItems.setPredicate(item -> true);
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
				handleTextFill(selectedValuesProperty);
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
	
	private void handleTextFill(List<? extends T> list)
	{
		if(list.size() == 0) setText("-");
		else if(list.size() == 1)
		{
			if(converter != null) setText(converter.toString(list.getFirst()));
			else setText(list.getFirst().toString());
		}
		else setText(list.size() + " values...");
	}
	
	public void setCellFactory(Callback<ListView<T>, ListCell<T>> callback)
	{
		searchList.setCellFactory(listView ->
		{
			ListCell<T> cell = callback.call(listView);
			cell.prefWidthProperty().bind(searchList.widthProperty());
			cell.setMaxWidth(USE_PREF_SIZE);
			cell.setOnMousePressed(e ->
			{
				if(selectedValuesProperty.size() == 1 && selectedValuesProperty.stream().anyMatch(val -> val != null && val.equals(cell.getItem())))
				{ selectedValuesProperty.clear(); }
				else if(!selectedValuesProperty.remove(cell.getItem())) selectedValuesProperty.add(cell.getItem());
			});
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
