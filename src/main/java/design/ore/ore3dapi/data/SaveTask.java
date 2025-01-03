package design.ore.ore3dapi.data;

import design.ore.ore3dapi.Util;
import design.ore.ore3dapi.Util.Log;
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
