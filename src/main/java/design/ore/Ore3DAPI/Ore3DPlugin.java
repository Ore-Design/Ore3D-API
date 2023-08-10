package design.ore.Ore3DAPI;

import org.pf4j.Plugin;
import org.pf4j.RuntimeMode;
import org.slf4j.Logger;

public abstract class Ore3DPlugin extends Plugin
{
	protected final PluginContext context;
	
	private static boolean isDebug;
	public static boolean isDebug() { return isDebug; }
	protected static Logger LOG;

    protected Ore3DPlugin(PluginContext context)
    {
        super();
        LOG = context.getLogger();
        this.context = context;
		isDebug = context.getRuntimeMode() == RuntimeMode.DEVELOPMENT;
    }
}
