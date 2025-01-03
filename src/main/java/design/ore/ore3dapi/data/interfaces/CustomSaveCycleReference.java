package design.ore.ore3dapi.data.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import design.ore.ore3dapi.Util;
import design.ore.ore3dapi.Util.Log;
import design.ore.ore3dapi.data.core.Transaction;
import design.ore.ore3dapi.data.wrappers.UpdatePacket;

public abstract class CustomSaveCycleReference
{
	List<Consumer<CustomSaveCycleReference>> listeners = new ArrayList<>();
	final String name;
	
	public CustomSaveCycleReference(String name) { this.name = name; }
	
	public void trigger()
	{
		for(Consumer<CustomSaveCycleReference> call : listeners)
		{
			try { call.accept(this); }
			catch(Exception e) { Log.getLogger().warn("An error occured while running listener for custom save cycle reference " + name + ": " + e.getMessage() + "\n" + Util.throwableToString(e)); }
		}
	}

	public void addListener(Consumer<CustomSaveCycleReference> call) { listeners.add(call); }
	public void removeListener(Consumer<CustomSaveCycleReference> call) { listeners.remove(call); }
	
	public abstract UpdatePacket generateUpdatePacket(String navigationID, Transaction transaction);
}
