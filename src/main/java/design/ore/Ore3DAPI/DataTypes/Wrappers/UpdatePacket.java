package design.ore.Ore3DAPI.DataTypes.Wrappers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.DataTypes.Tuple;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

public class UpdatePacket
{
	public UpdatePacket(String title, boolean canRunSimultaneaously) { this(title, canRunSimultaneaously, null); }
	
	public UpdatePacket(String title, boolean canRunSimultaneaously, Callable<UpdatePacket> generateNextUpdatePacket)
	{
		this.title = title;
		this.updateTasks = FXCollections.observableArrayList();
		this.runSimultaneaously = canRunSimultaneaously;
		this.generateNextUpdatePacket = generateNextUpdatePacket;
		
		maxProgressProperty = new ReadOnlyDoubleWrapper();
		maxProgressProperty.bind(Bindings.createDoubleBinding(() -> (double) updateTasks.size(), updateTasks));
		
		progressBinding = new SimpleDoubleProperty(0).add(0);
		
		totalProgressBinding = progressBinding.divide(maxProgressProperty);
		
		isCompleteBinding = totalProgressBinding.greaterThanOrEqualTo(1.0).and(maxProgressProperty.greaterThan(0));
		
		this.updateTasks.addListener((ListChangeListener.Change<? extends Tuple<String, String, Task<?>>> c) ->
		{
			while(c.next())
			{
				for(Tuple<String, String, Task<?>> added : c.getAddedSubList())
				{
					Task<?> addedTask = added.getThird();
					if(progressBinding == null) progressBinding = Bindings.createDoubleBinding(() ->
						addedTask.progressProperty().get() < 0 ? 0.0 : addedTask.progressProperty().get(), addedTask.progressProperty());
					else progressBinding = progressBinding.add(Bindings.createDoubleBinding(() ->
						addedTask.progressProperty().get() < 0 ? 0.0 : addedTask.progressProperty().get(), addedTask.progressProperty()));
				}
				for(Tuple<String, String, Task<?>> removed : c.getRemoved())
				{
					Task<?> removedTask = removed.getThird();
					if(progressBinding != null) progressBinding = progressBinding.subtract(removedTask.progressProperty());
				}
				
				totalProgressBinding = progressBinding.divide(maxProgressProperty);
				
				isCompleteBinding = totalProgressBinding.greaterThanOrEqualTo(1.0).and(maxProgressProperty.greaterThan(0));
			}
		});
	}
	
	@Getter String title;
	boolean runSimultaneaously;
	public boolean canRunSimultaneaously() { return runSimultaneaously; }
	@Getter ObservableList<Tuple<String, String, Task<?>>> updateTasks;
	@Getter DoubleBinding totalProgressBinding;
	DoubleBinding progressBinding; 
	ReadOnlyDoubleWrapper maxProgressProperty;
	public ReadOnlyDoubleProperty getMaxProgressProperty() { return maxProgressProperty.getReadOnlyProperty(); }
	@Getter BooleanBinding isCompleteBinding;
	@Setter private Callable<UpdatePacket> generateNextUpdatePacket;
	public UpdatePacket getNextUpdate()
	{
		if(generateNextUpdatePacket == null) return null;
		
		try { return generateNextUpdatePacket.call(); }
		catch (Exception e)
		{
			Util.Log.getLogger().warn("An error occured while creating the next update packet!");
			return null;
		}
	}
	@Getter private final List<Pair<String, Runnable>> generateOnCompleteButtons = new ArrayList<>();
	
	public void addTask(String title, String subtitle, Task<?> updateTask) { updateTasks.add(new Tuple<>(title, subtitle, updateTask)); }
	
	public void run(ExecutorService executor)
	{
		List<Callable<Object>> toRun = new ArrayList<>();
		for(Tuple<String, String, Task<?>> tuple : updateTasks)
		{
			Task<?> t = tuple.getThird();
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
