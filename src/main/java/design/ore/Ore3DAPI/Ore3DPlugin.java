package design.ore.Ore3DAPI;

import org.pf4j.Plugin;

public abstract class Ore3DPlugin extends Plugin
{
	protected final PluginContext context;

    protected Ore3DPlugin(PluginContext context)
    {
        super();
        this.context = context;
    }
    
    public abstract String targetVersion();
}
