package design.ore.Ore3DAPI.DataTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.util.Pair;
import lombok.Getter;

public class UpdatePacket
{
	public UpdatePacket(String title, boolean canRunSimultaneaously)
	{
		this.title = title;
		this.updateTasks = FXCollections.observableHashMap();
		this.runSimultaneaously = canRunSimultaneaously;
		
		maxProgressProperty = new ReadOnlyDoubleWrapper();
		maxProgressProperty.bind(Bindings.createDoubleBinding(() -> (double) updateTasks.size(), updateTasks));
		
		progressBinding = new SimpleDoubleProperty(0).add(0);
		
		totalProgressBinding = progressBinding.divide(maxProgressProperty);
		
		isCompleteBinding = totalProgressBinding.greaterThanOrEqualTo(1.0).and(maxProgressProperty.greaterThan(0));
		
		this.updateTasks.addListener(new MapChangeListener<Pair<String, String>, Task<?>>()
		{
			@Override
			public void onChanged(Change<? extends Pair<String, String>, ? extends Task<?>> c)
			{
				Task<?> added = c.getValueAdded();
				Task<?> removed = c.getValueRemoved();
				if(added != null)
				{
					if(progressBinding == null) progressBinding = Bindings.createDoubleBinding(() -> added.progressProperty().get() < 0 ? 0.0 : added.progressProperty().get(), added.progressProperty());
					else progressBinding = progressBinding.add(Bindings.createDoubleBinding(() -> added.progressProperty().get() < 0 ? 0.0 : added.progressProperty().get(), added.progressProperty()));
				}
				
				if(removed != null)
				{
					if(progressBinding != null) progressBinding = progressBinding.subtract(removed.progressProperty());
				}
				
				totalProgressBinding = progressBinding.divide(maxProgressProperty);
				
				isCompleteBinding = totalProgressBinding.greaterThanOrEqualTo(1.0).and(maxProgressProperty.greaterThan(0));
			}
		});
	}
	
	@Getter String title;
	boolean runSimultaneaously;
	public boolean canRunSimultaneaously() { return runSimultaneaously; }
	@Getter ObservableMap<Pair<String, String>, Task<?>> updateTasks; 
	@Getter DoubleBinding totalProgressBinding;
	DoubleBinding progressBinding; 
	ReadOnlyDoubleWrapper maxProgressProperty;
	public ReadOnlyDoubleProperty getMaxProgressProperty() { return maxProgressProperty.getReadOnlyProperty(); }
	@Getter BooleanBinding isCompleteBinding;
	
	public void addTask(String title, String subtitle, Task<?> updateTask) { updateTasks.put(new Pair<>(title, subtitle), updateTask); }
	
	public void run(ExecutorService executor)
	{
		List<Callable<Object>> toRun = new ArrayList<>();
		for(Task<?> t : updateTasks.values())
		{
			toRun.add(new Callable<Object>()
			{
				@Override
				public Object call() throws Exception
				{
					t.run();
					return t.getValue();
				}
			});
		}
		
		try { executor.invokeAll(toRun); }
		catch (InterruptedException e) { e.printStackTrace(); }
	}
}
