package design.ore.ore3dapi;

import java.util.Map;
import java.util.Set;

import org.pf4j.RuntimeMode;

import lombok.Getter;

public class PluginContext
{
	@Getter private final RuntimeMode runtimeMode;
	@Getter private final boolean debug;
	@Getter private final Map<String, String> valueMap;
	@Getter private final boolean sandbox;
	@Getter private final String ore3DVersion;
	@Getter private final Set<String> permissionFlags;
	
	public PluginContext(RuntimeMode runtimeMode, boolean debug, Map<String, String> valueMap, boolean sandbox, String version, Set<String> permissionFlags)
	{
		this.runtimeMode = runtimeMode;
		this.debug = debug;
		this.valueMap = valueMap;
		this.sandbox = sandbox;
		this.ore3DVersion = version;
		this.permissionFlags = permissionFlags;
	}
	
	public String getValue(String key) { return valueMap.get(key); }
}
