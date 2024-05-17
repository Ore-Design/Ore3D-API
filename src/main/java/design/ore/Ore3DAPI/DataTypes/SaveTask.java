package design.ore.Ore3DAPI.DataTypes;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import javafx.concurrent.Task;

public abstract class SaveTask<T> extends Task<T>
{
	@Override
	protected void failed()
	{
		super.failed();
		
		updateProgress(100, 100);
		updateMessage("Failed - " + getException().getMessage());
		Log.getLogger().warn(Util.formatThrowable("Update taks failed!", getException()));
	}
}
