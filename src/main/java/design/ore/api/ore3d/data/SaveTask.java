package design.ore.api.ore3d.data;

import design.ore.api.ore3d.Util;
import design.ore.api.ore3d.Util.Log;
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
