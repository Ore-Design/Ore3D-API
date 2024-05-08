package design.ore.Ore3DAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.Util.Mapper;
import design.ore.Ore3DAPI.DataTypes.StoredValue;
import design.ore.Ore3DAPI.DataTypes.Interfaces.CustomButtonReference;
import design.ore.Ore3DAPI.DataTypes.Interfaces.CustomSaveCycleReference;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.MiscEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.DataTypes.Wrappers.CatalogItem;
import lombok.Getter;
import lombok.Setter;

public class Registry
{
	public static void registerLogger(Logger log) { if(Log.logger == null) { Log.logger = log; } else { Log.logger.warn("Someone attempted to register a different logger, but it's locked!"); } }
	
	public static void registerMapperFactory(Callable<ObjectMapper> mapperFactory)
	{
		if(Mapper.mapperFactory == null)
		{
			Mapper.mapperFactory = mapperFactory;
			Mapper.mapper = Mapper.createMapper();
		}
		else { Log.logger.warn("Someone attempted to register a different mapper factory, but it's locked!"); }
	}
	
	@Getter private static final List<ClassLoader> registeredClassLoaders = new ArrayList<ClassLoader>();
	public static void registerClassLoader(ClassLoader cl) { registeredClassLoaders.add(cl); }
	
	@Getter private static final Map<String, StoredValue> registeredMiscEntryStoredValues = new HashMap<>();
	public static void registerMiscEntryStoredValues(String mapID, StoredValue value)
	{
		if(registeredMiscEntryStoredValues.containsKey(mapID)) Log.getLogger().warn("A misc entry stored value with the ID " + mapID + " has already been registered! Overriding...");
		registeredMiscEntryStoredValues.put(mapID, value);
	}
	
	@Getter private static final Map<String, StoredValue> registeredBOMEntryStoredValues = new HashMap<>();
	public static void registerBOMEntryStoredValues(String mapID, StoredValue value)
	{
		if(registeredBOMEntryStoredValues.containsKey(mapID)) Log.getLogger().warn("A BOM entry stored value with the ID " + mapID + " has already been registered! Overriding...");
		registeredBOMEntryStoredValues.put(mapID, value);
	}
	
	@Getter private static final Map<String, StoredValue> registeredRoutingEntryStoredValues = new HashMap<>();
	public static void registerRoutingEntryStoredValues(String mapID, StoredValue value)
	{
		if(registeredRoutingEntryStoredValues.containsKey(mapID)) Log.getLogger().warn("A routing entry stored value with the ID " + mapID + " has already been registered! Overriding...");
		registeredRoutingEntryStoredValues.put(mapID, value);
	}
	
	@Getter private static final List<CustomButtonReference> registeredCustomEditButtons = new ArrayList<>();
	public static void registerCustomEditButton(CustomButtonReference button)
	{
		registeredCustomEditButtons.add(button);
	}
	
	@Getter private static final Map<String, CustomSaveCycleReference> registeredCustomSaveCycles = new HashMap<>();
	public static void registerCustomSaveCycle(String saveCycleID, CustomSaveCycleReference cycle)
	{
		if(registeredCustomSaveCycles.containsKey(saveCycleID)) Log.getLogger().warn("A save cycle already exists with ID " + saveCycleID + "! It will be overridden!");
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
				Log.getLogger().warn("Failed to retrieve BOM Entries because " + e.getMessage() + "\n" + Util.stackTraceArrayToString(e));
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
				Log.getLogger().warn("Failed to retrieve Routing Entries because " + e.getMessage() + "\n" + Util.stackTraceArrayToString(e));
				return new HashMap<>();
			}
		}
	}
	
	private static Map<String, Consumer<Build>> registeredBuildDuplicateHandlers = new HashMap<>();
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
	
	private static Map<String, Consumer<BOMEntry>> registeredBOMDuplicateHandlers = new HashMap<>();
	public static void registerBOMDuplicateHandler(String handlerID, Consumer<BOMEntry> handler)
	{
		if(registeredBOMDuplicateHandlers.containsKey(handlerID)) Log.getLogger().warn("BOMEntry Duplicate Handler with ID " + handlerID + " is already registered! Replacing...");
		registeredBOMDuplicateHandlers.put(handlerID, handler);
	}
	public static void handleBOMDuplicate(BOMEntry entry) { registeredBOMDuplicateHandlers.values().forEach(handler -> handler.accept(entry)); }
	
	private static Map<String, Consumer<RoutingEntry>> registeredRoutingDuplicateHandlers = new HashMap<>();
	public static void registerRoutingDuplicateHandler(String handlerID, Consumer<RoutingEntry> handler)
	{
		if(registeredRoutingDuplicateHandlers.containsKey(handlerID)) Log.getLogger().warn("RoutingEntry Duplicate Handler with ID " + handlerID + " is already registered! Replacing...");
		registeredRoutingDuplicateHandlers.put(handlerID, handler);
	}
	public static void handleRoutingDuplicate(RoutingEntry entry) { registeredRoutingDuplicateHandlers.values().forEach(handler -> handler.accept(entry)); }
	
	private static Map<String, Consumer<MiscEntry>> registeredMiscDuplicateHandlers = new HashMap<>();
	public static void registerMiscDuplicateHandler(String handlerID, Consumer<MiscEntry> handler)
	{
		if(registeredMiscDuplicateHandlers.containsKey(handlerID)) Log.getLogger().warn("MiscEntry Duplicate Handler with ID " + handlerID + " is already registered! Replacing...");
		registeredMiscDuplicateHandlers.put(handlerID, handler);
	}
	public static void handleMiscDuplicate(MiscEntry entry) { registeredMiscDuplicateHandlers.values().forEach(handler -> handler.accept(entry)); }
}
