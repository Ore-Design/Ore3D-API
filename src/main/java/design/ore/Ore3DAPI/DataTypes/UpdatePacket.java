package design.ore.Ore3DAPI.DataTypes;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class UpdatePacket
{
	public UpdatePacket(String title, String subtitle)
	{
		this(title, subtitle, FXCollections.observableArrayList());
	}
	
	public UpdatePacket(String title, String subtitle, ObservableList<Task<Boolean>> tasks)
	{
		this.title = title;
		this.subtitle = subtitle;
		this.updateTasks = tasks;
		
		maxProgressBinding = new ReadOnlyDoubleWrapper();
		maxProgressBinding.bind(Bindings.createDoubleBinding(() -> (double) updateTasks.size(), updateTasks));
		
		progressBinding = new SimpleDoubleProperty(0).add(0);
		
		for(Task<Boolean> tsk : tasks)
		{
			if(progressBinding == null) progressBinding = tsk.progressProperty().add(0);
			else progressBinding = progressBinding.add(tsk.progressProperty());
		}
	}
	
	@SafeVarargs
	public UpdatePacket(String title, String subtitle, Task<Boolean>... tasks)
	{
		this.title = title;
		this.subtitle = subtitle;
		this.updateTasks = FXCollections.observableArrayList();
		
		maxProgressBinding = new ReadOnlyDoubleWrapper();
		maxProgressBinding.bind(Bindings.createDoubleBinding(() -> (double) updateTasks.size(), updateTasks));
		
		progressBinding = new SimpleDoubleProperty(0).add(0);
		
		for(Task<Boolean> tsk : tasks)
		{
			updateTasks.add(tsk);
			if(progressBinding == null) progressBinding = tsk.progressProperty().add(0);
			else progressBinding = progressBinding.add(tsk.progressProperty());
		}
	}
	
	String title;
	String subtitle;
	ObservableList<Task<Boolean>> updateTasks;
	DoubleBinding progressBinding;
	ReadOnlyDoubleWrapper maxProgressBinding;
	
	public void addTask(Task<Boolean> updateTask) { updateTasks.add(updateTask); }
}
