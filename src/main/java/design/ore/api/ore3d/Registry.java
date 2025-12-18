package design.ore.api.ore3d;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;

import design.ore.api.ore3d.Util.Log;
import design.ore.api.ore3d.Util.Mapper;
import design.ore.api.ore3d.data.PrecheckEvent;
import design.ore.api.ore3d.data.StoredValue;
import design.ore.api.ore3d.data.core.Build;
import design.ore.api.ore3d.data.core.Transaction;
import design.ore.api.ore3d.data.interfaces.CustomButtonReference;
import design.ore.api.ore3d.data.interfaces.CustomSaveCycleReference;
import design.ore.api.ore3d.data.pricing.BOMEntry;
import design.ore.api.ore3d.data.pricing.MiscEntry;
import design.ore.api.ore3d.data.pricing.PricingData;
import design.ore.api.ore3d.data.pricing.RoutingEntry;
import design.ore.api.ore3d.data.wrappers.CatalogItem;
import lombok.Getter;
import lombok.Setter;

public class Registry
{
	public static void registerMapperFactory(Callable<ObjectMapper> mapperFactory)
	{
		if(Mapper.mapperFactory == null)
		{
			Mapper.mapperFactory = mapperFactory;
			Mapper.mapper = Mapper.createMapper();
		}
		else { Log.getLogger().warn("Someone attempted to register a different mapper factory, but it's locked!"); }
	}

    @Getter private static Supplier<PricingData> pricingDataSupplier = null;
    public static void registerPricingDataSupplier(Supplier<PricingData> supplier)
    {
        if (pricingDataSupplier != null) Log.getLogger().warn("Someone attempted to initialize the pricing data supplier, but it is already initialized! Skipping...");
        else pricingDataSupplier = supplier;
    }
	
	@Getter private static final List<ClassLoader> registeredClassLoaders = new ArrayList<ClassLoader>();
	public static void registerClassLoader(ClassLoader cl) { registeredClassLoaders.add(cl); }
	
	@Getter private static final Map<String, StoredValue> registeredMiscEntryStoredValues = new HashMap<>();
	public static void registerMiscEntryStoredValues(String mapID, StoredValue value)
	{
		if(registeredMiscEntryStoredValues.containsKey(mapID)) Log.getLogger().warn("A misc entry stored value with the ID {} has already been registered! Overriding...", mapID);
		registeredMiscEntryStoredValues.put(mapID, value);
	}
	
	@Getter private static final Map<String, StoredValue> registeredBOMEntryStoredValues = new HashMap<>();
	public static void registerBOMEntryStoredValues(String mapID, StoredValue value)
	{
		if(registeredBOMEntryStoredValues.containsKey(mapID)) Log.getLogger().warn("A BOM entry stored value with the ID {} has already been registered! Overriding...", mapID);
		registeredBOMEntryStoredValues.put(mapID, value);
	}
	
	@Getter private static final Map<String, StoredValue> registeredRoutingEntryStoredValues = new HashMap<>();
	public static void registerRoutingEntryStoredValues(String mapID, StoredValue value)
	{
		if(registeredRoutingEntryStoredValues.containsKey(mapID)) Log.getLogger().warn("A routing entry stored value with the ID " + mapID + " has already been registered! Overriding...");
		registeredRoutingEntryStoredValues.put(mapID, value);
	}
	
	@Getter private static final Map<String, CustomButtonReference> registeredCustomEditButtons = new HashMap<>();
	public static void registerCustomEditButton(String editButtonId, CustomButtonReference button)
	{
		if(registeredCustomEditButtons.containsKey(editButtonId)) Log.getLogger().warn("A custom edit button already exists with ID " + editButtonId + "! Overriding...");
		registeredCustomEditButtons.put(editButtonId, button);
	}
	
	@Getter private static final Map<String, CustomSaveCycleReference> registeredCustomSaveCycles = new HashMap<>();
	public static void registerCustomSaveCycle(String saveCycleID, CustomSaveCycleReference cycle)
	{
		if(registeredCustomSaveCycles.containsKey(saveCycleID)) Log.getLogger().warn("A save cycle already exists with ID " + saveCycleID + "! Overriding...");
		registeredCustomSaveCycles.put(saveCycleID, cycle);
	}
	
	@Getter private static final Map<String, Runnable> registeredCommands = new HashMap<>();
	public static void registerCommand(String command, Runnable action)
	{
		registeredCommands.put(command.toLowerCase(), action);
	}
	
	@Getter private static final List<CatalogItem> registeredCatalogItems = new ArrayList<>();
	public static void registerCatalogItem(CatalogItem item) { registeredCatalogItems.add(item); }
	public static void registerCatalogItems(Collection<CatalogItem> items) { registeredCatalogItems.addAll(items); }

	@Getter @Setter private static boolean customChildrenPreventCatalogParents = false;
	@Getter @Setter private static boolean childrenOnlyCatalogIfParentIsCatalog = false;
	
	private static Callable<Map<String, BOMEntry>> bomEntryAccessor;
	public static final void initializeBOMEntriesAccess(Callable<Map<String, BOMEntry>> accessor)
	{
		if(bomEntryAccessor == null) bomEntryAccessor = accessor;
		else Log.getLogger().warn("Attempted to initialize BOM Entry Accessor, but it has already been initialized!");
	}
	public static Map<String, BOMEntry> getBOMEntries()
	{
		if(bomEntryAccessor == null)
		{
			Log.getLogger().warn("Unable to access BOM Entries, as they have not yet been initialized!");
			return new HashMap<>();
		}
		else
		{
			try { return bomEntryAccessor.call(); }
			catch(Exception e)
			{
				Log.getLogger().warn("Failed to retrieve BOM Entries!", e);
				return new HashMap<>();
			}
		}
	}
	
	private static Callable<Map<String, RoutingEntry>> routingEntryAccessor;
	public static final void initializeRoutingEntriesAccess(Callable<Map<String, RoutingEntry>> accessor)
	{
		if(routingEntryAccessor == null) routingEntryAccessor = accessor;
		else Log.getLogger().warn("Attempted to initialize Routing Entry Accessor, but it has already been initialized!");
	}
	public static Map<String, RoutingEntry> getRoutingEntries()
	{
		if(routingEntryAccessor == null)
		{
			Log.getLogger().warn("Unable to access Routing Entries, as they have not yet been initialized!");
			return new HashMap<>();
		}
		else
		{
			try { return routingEntryAccessor.call(); }
			catch(Exception e)
			{
				Log.getLogger().warn("Failed to retrieve Routing Entries!", e);
				return new HashMap<>();
			}
		}
	}
	
	private static final Map<String, Consumer<Build>> registeredBuildDuplicateHandlers = new HashMap<>();
	public static void registerBuildDuplicateHandler(String handlerID, Consumer<Build> handler)
	{
		if(registeredBuildDuplicateHandlers.containsKey(handlerID)) Log.getLogger().warn("Build Duplicate Handler with ID " + handlerID + " is already registered! Replacing...");
		registeredBuildDuplicateHandlers.put(handlerID, handler);
	}
	public static void handleBuildDuplicate(Build build) { handleBuildDuplicateRecursive(build); }
	private static void handleBuildDuplicateRecursive(Build build)
	{
		registeredBuildDuplicateHandlers.values().forEach(handler -> handler.accept(build));
		for(Build cb : build.getChildBuilds()) handleBuildDuplicateRecursive(cb);
	}
	
	private static final Map<String, Consumer<BOMEntry>> registeredBOMDuplicateHandlers = new HashMap<>();
	public static void registerBOMDuplicateHandler(String handlerID, Consumer<BOMEntry> handler)
	{
		if(registeredBOMDuplicateHandlers.containsKey(handlerID)) Log.getLogger().warn("BOMEntry Duplicate Handler with ID " + handlerID + " is already registered! Replacing...");
		registeredBOMDuplicateHandlers.put(handlerID, handler);
	}
	public static void handleBOMDuplicate(BOMEntry entry) { registeredBOMDuplicateHandlers.values().forEach(handler -> handler.accept(entry)); }
	
	private static final Map<String, Consumer<RoutingEntry>> registeredRoutingDuplicateHandlers = new HashMap<>();
	public static void registerRoutingDuplicateHandler(String handlerID, Consumer<RoutingEntry> handler)
	{
		if(registeredRoutingDuplicateHandlers.containsKey(handlerID)) Log.getLogger().warn("RoutingEntry Duplicate Handler with ID " + handlerID + " is already registered! Replacing...");
		registeredRoutingDuplicateHandlers.put(handlerID, handler);
	}
	public static void handleRoutingDuplicate(RoutingEntry entry) { registeredRoutingDuplicateHandlers.values().forEach(handler -> handler.accept(entry)); }
	
	private static final Map<String, Consumer<MiscEntry>> registeredMiscDuplicateHandlers = new HashMap<>();
	public static void registerMiscDuplicateHandler(String handlerID, Consumer<MiscEntry> handler)
	{
		if(registeredMiscDuplicateHandlers.containsKey(handlerID)) Log.getLogger().warn("MiscEntry Duplicate Handler with ID " + handlerID + " is already registered! Replacing...");
		registeredMiscDuplicateHandlers.put(handlerID, handler);
	}
	public static void handleMiscDuplicate(MiscEntry entry) { registeredMiscDuplicateHandlers.values().forEach(handler -> handler.accept(entry)); }

	private static final Map<String, Consumer<PrecheckEvent>> registeredSavePrechecks = new HashMap<>();
	public static void registerSavePrecheck(String id, Consumer<PrecheckEvent> precheck)
	{
		if(registeredSavePrechecks.containsKey(id)) Log.getLogger().warn("Save Precheck with ID " + id + " is already registered! Replacing...");
		registeredSavePrechecks.put(id, precheck);
	}
	public static boolean unregisterSavePrecheck(String id) { return registeredSavePrechecks.remove(id) != null; }
	public static boolean runPrechecks(Transaction tran)
	{
		PrecheckEvent event = new PrecheckEvent(tran);
		for(Consumer<PrecheckEvent> precheck : registeredSavePrechecks.values())
		{
			precheck.accept(event);
			if(event.isInterrupted()) return false;
		}
		
		return true;
	}
}
