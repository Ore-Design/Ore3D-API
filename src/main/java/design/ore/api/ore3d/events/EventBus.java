package design.ore.api.ore3d.events;

/**
 * Manages the organization and firing
 * of {@link IEvent events} within the
 * Ore3D application lifecycle.
 */
public abstract class EventBus
{
    protected static EventBus INSTANCE;
    protected EventBus() { INSTANCE = this; }
    public static <T extends IEvent> void fire(Class<T> eventType, T event) { if(INSTANCE != null) INSTANCE.fireEvent(eventType, event);}

    /**
     * Calls the EventBus static instance to fire an event.
     * All events registered through the
     * {@link design.ore.api.ore3d.plugin.IOre3DPluginContext#registerEventHandler registerEventHandler}
     * method of the same event type will receive the event instance.
     *
     * @param eventType a {@link Class} object of a class that implements {@link IEvent}.
     * @param event     an instance of the event.
     */
    protected abstract <T extends IEvent> void fireEvent(Class<T> eventType, T event);
}