package design.ore.Ore3DAPI;

import java.util.Map;

import org.pf4j.RuntimeMode;

import lombok.Getter;

public class PluginContext
{
	@Getter private final RuntimeMode runtimeMode;
	@Getter private final boolean debug;
	@Getter private final Map<String, String> valueMap;
	@Getter private final boolean sandbox;
	
	public PluginContext(RuntimeMode runtimeMode, boolean debug, Map<String, String> valueMap, boolean sandbox)
	{
		this.runtimeMode = runtimeMode;
		this.debug = debug;
		this.valueMap = valueMap;
		this.sandbox = sandbox;
	}
	
	public String getValue(String key) { return valueMap.get(key); }
}
