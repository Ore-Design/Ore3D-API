package design.ore.api.ore3d.data.interfaces;

import design.ore.api.ore3d.data.specs.Spec;
import javafx.scene.Node;

import java.util.List;

public interface ISpecUI<T>
{
	public void rebindUI(String popoutID);
	public void rebindMultiUI(List<Spec<T>> specs, String popoutID);
	public void unbindUI();
	public Node getUINode();
	public boolean isBound();
	public void setBound();
	public default void rebind(List<Spec<T>> specs, String popoutID)
	{
		if(isBound()) throw new IllegalStateException("Already bound UI cannot be rebound!");
		else
		{
			if(specs == null || specs.size() <= 0) rebindUI(popoutID);
			else rebindMultiUI(specs, popoutID);
			setBound();
		}
	}
}
