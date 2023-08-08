package design.ore.Ore3DAPI.Extensions;

import org.pf4j.ExtensionPoint;

public interface PluginData extends ExtensionPoint
{
	/*
	 * Retrieves the target version for the plugin.
	 * 
	 * @return       the major and minor version the plugin should target, ie '2.4'.
	 */
	String getTargetVersion();
}