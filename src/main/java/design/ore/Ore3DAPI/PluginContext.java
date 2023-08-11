package design.ore.Ore3DAPI;

import java.util.Map;

import org.pf4j.RuntimeMode;
import org.slf4j.Logger;

public class PluginContext
{
	private final RuntimeMode runtimeMode;
	private final Logger logger;
	private final boolean debug;
	private final Map<String, String> valueMap;
	private final boolean sandbox;
	
	public PluginContext(RuntimeMode runtimeMode, Logger logger, boolean debug, Map<String, String> valueMap, boolean sandbox)
	{
		this.runtimeMode = runtimeMode;
		this.logger = logger;
		this.debug = debug;
		this.valueMap = valueMap;
		this.sandbox = sandbox;
	}
	
	public RuntimeMode getRuntimeMode() { return runtimeMode; }
	
	public Logger getLogger() { return logger; }

	public boolean isDebug() { return debug; }
	public boolean isSandbox() { return sandbox; }
	public Map<String, String> getValueMap() { return valueMap; }
	public String getValue(String key) { return valueMap.get(key); }
}
