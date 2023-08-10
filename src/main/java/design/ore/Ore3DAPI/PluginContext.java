package design.ore.Ore3DAPI;

import org.pf4j.RuntimeMode;
import org.slf4j.Logger;

public class PluginContext
{
	private final RuntimeMode runtimeMode;
	private final Logger logger;
	
	public PluginContext(RuntimeMode runtimeMode, Logger logger)
	{
		this.runtimeMode = runtimeMode;
		this.logger = logger;
	}
	
	public RuntimeMode getRuntimeMode() { return runtimeMode; }
	
	public Logger getLogger() { return logger; }
}
