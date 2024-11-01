package design.ore.Ore3DAPI.ui;

import java.util.Collection;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
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

public class SearchableDropdown<T> extends TextField
{
	@Getter final ObservableList<T> items;
	final FilteredList<T> filteredItems;
	public void setItems(Collection<T> items) { this.items.setAll(items); }
	
	private final SimpleObjectProperty<T> valueProperty = new SimpleObjectProperty<>();
	public void setValue(T value) { valueProperty.set(value); }
	public T getValue() { return valueProperty.get(); }
	public void clearSelection() { searchList.getSelectionModel().clearSelection(); }
	public SimpleObjectProperty<T> valueProperty() { return valueProperty; }
	private final ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> translatePopup();
	private final EventHandler<? super ScrollEvent> scrollEvent = e -> translatePopup();
	
	private final BooleanBinding paneVisibleBinding;
	
	@Getter @Setter private StringConverter<T> converter;
	
	private final ListView<T> searchList;
	
	public SearchableDropdown() { this(FXCollections.observableArrayList()); }
	
	private final SearchableDropdown<T> INSTANCE;
	
	public SearchableDropdown(ObservableList<T> items)
	{
		super();
		INSTANCE = this;
		this.items = items;
		
		setPromptText("-");
		
		filteredItems = this.items.filtered(item -> true);
		
		searchList = new ListView<>(filteredItems);
		searchList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		searchList.prefWidthProperty().bind(widthProperty());
		searchList.setMaxWidth(USE_PREF_SIZE);
		searchList.getStyleClass().add("searchable-dropdown-list");
		searchList.setMaxHeight(300);
		
		paneVisibleBinding = focusedProperty().or(searchList.focusedProperty());
		searchList.visibleProperty().bind(paneVisibleBinding);
		
		valueProperty.addListener((obs, oldVal, newVal) ->
		{
			if(newVal != null) setText(converter.toString(newVal));
			else setText(getPromptText());
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
				if(valueProperty.get() == null) setText(getPromptText());
				else { setText(converter.toString(valueProperty.get())); }
				if(getScene() != null) getScene().removeEventFilter(ScrollEvent.ANY, scrollEvent);
			}
			else if(newVal)
			{
				if(getScene() != null) getScene().addEventFilter(ScrollEvent.ANY, scrollEvent);
				setText("");
				translatePopup();
				searchList.getSelectionModel().select(getValue());
			}
		});
	}
	
	public void setCellFactory(Callback<ListView<T>, ListCell<T>> callback)
	{
		searchList.setCellFactory(listView ->
		{
			ListCell<T> cell = callback.call(listView);
			cell.prefWidthProperty().bind(searchList.widthProperty());
			cell.setMaxWidth(USE_PREF_SIZE);
			cell.setOnMouseClicked(e ->
			{
				setValue(cell.getItem());
				INSTANCE.getParent().requestFocus();
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