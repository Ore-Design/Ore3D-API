package design.ore.api.ore3d.plugin;

import ch.qos.logback.classic.Logger;
import design.ore.api.ore3d.events.IEvent;
import design.ore.api.ore3d.events.IEventReference;
import design.ore.api.ore3d.extensions.CRMEndpoint;
import design.ore.api.ore3d.extensions.Dataset;

import java.io.File;
import java.util.Set;
import java.util.function.Consumer;

public interface IOre3DPluginContext
{
    Logger getLog();
    File getPluginDataDirectory();

    boolean isDebug();
    boolean isSandbox();
    Set<String> getPermissionFlags();
    String getValue(String key);

    void registerCrmEndpoint(CRMEndpoint endpoint);
    void registerDataset(Dataset endpoint);

    <T extends IEvent> IEventReference<T> registerEventHandler(Class<T> eventClass, Consumer<T> eventHandler);
}
