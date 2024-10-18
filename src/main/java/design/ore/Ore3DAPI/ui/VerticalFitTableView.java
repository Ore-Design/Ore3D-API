package design.ore.Ore3DAPI.ui;

import java.util.Optional;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.util.Callback;

public abstract class VerticalFitTableView<T> extends TableView<T>
{
	InvalidationListener cellSizeListener = (inv) -> fitHeight();
	
	protected final ObservableList<TableRow<T>> registeredRows = FXCollections.observableArrayList();
	private final ObservableList<TableRow<T>> sizingRows = FXCollections.observableArrayList();
	
	TableHeaderRow headerRow = null;
	
	public VerticalFitTableView()
	{
		super();
		
		skinProperty().addListener((obs, oldVal, newVal) ->
		{
			if(newVal != null && newVal instanceof TableViewSkin)
			{
				@SuppressWarnings("unchecked")
				Optional<Node> header = ((TableViewSkin<T>) getSkin()).getChildren().stream().filter(ch -> ch != null && ch instanceof TableHeaderRow).findFirst();
				if(header.isPresent()) headerRow = (TableHeaderRow) header.get();
			}
		});
		
		setFixedCellSize(25);
		
		registeredRows.addListener((Change<? extends TableRow<T>> c) ->
		{
			while(c.next())
			{
				if(c.getList().size() < getItems().size()) continue;
				
				sizingRows.clear();
				for(T item : getItems())
				{
					Optional<? extends TableRow<T>> matchingRow = c.getList().stream().filter(row -> row.getItem() != null && row.getItem().equals(item)).findFirst();
					if(matchingRow.isPresent()) sizingRows.add(matchingRow.get());
					else Log.getLogger().debug("Unable to find matching row for " + item);
				}
			}
		});
		
		sizingRows.addListener((Change<? extends TableRow<T>> c) -> fitHeight());
		
		setRowFactory(createRowFactoryWithRowRegistry());
		setMinHeight(USE_PREF_SIZE);
	}
	
	protected abstract Callback<TableView<T>, TableRow<T>> createRowFactoryWithRowRegistry();
	
	private void fitHeight()
	{
		DoubleBinding heightBinding = null;
		if(headerRow != null) heightBinding = Bindings.createDoubleBinding(() -> headerRow.getHeight() + (getHeight() > headerRow.getHeight() ? 0 : 5 * sizingRows.size()), headerRow.heightProperty());
		else heightBinding = Util.zeroDoubleBinding();
		
		for(TableRow<?> row : sizingRows)
		{
			heightBinding = heightBinding.add(row.heightProperty());
		}
		
		prefHeightProperty().bind(heightBinding);
	}
}
