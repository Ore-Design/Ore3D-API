package design.ore.Ore3DAPI;

import org.pf4j.RuntimeMode;
import org.slf4j.Logger;

public class PluginContext
{
	private final RuntimeMode runtimeMode;
	private final Logger logger;
	private final boolean debug;
	
	public PluginContext(RuntimeMode runtimeMode, Logger logger, boolean debug)
	{
		this.runtimeMode = runtimeMode;
		this.logger = logger;
		this.debug = debug;
	}
	
	public RuntimeMode getRuntimeMode() { return runtimeMode; }
	
	public Logger getLogger() { return logger; }
	
	public boolean isDebug() { return debug; }
}
