package design.ore.Ore3DAPI.Extensions;

import org.pf4j.ExtensionPoint;

import design.ore.Ore3DAPI.DataTypes.Version;

public interface PluginData extends ExtensionPoint
{
	/*
	 * Retrieves the Ore3D target version for the plugin.
	 * 
	 * @return       the major and minor version the plugin should target, ie '2.4'.
	 */
	Version getTargetVersion();
	
	/*
	 * Retrieves the logging prefix for the plugin.
	 * 
	 * @return       the logging prefix for the plugin, ie '[MyPlugin]'.
	 */
	String getPluginPrefix();
}