package design.ore.ore3dapi;

import org.pf4j.Plugin;

public abstract class Ore3DPlugin extends Plugin
{
	protected final PluginContext context;

    protected Ore3DPlugin(PluginContext context)
    {
        super();
        this.context = context;
    }
}
