package design.ore.api.ore3d.events;

import java.util.function.Consumer;

public interface IEventReference<T extends IEvent>
{
    Consumer<T> getEventHandler();

    boolean unregister();
}
