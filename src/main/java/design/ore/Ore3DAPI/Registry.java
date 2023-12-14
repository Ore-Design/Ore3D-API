package design.ore.Ore3DAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.Util.Mapper;
import design.ore.Ore3DAPI.DataTypes.StoredValue;
import lombok.Getter;

public class Registry
{
	public static void registerLogger(Logger log) { if(Log.logger == null) { Log.logger = log; } else { Log.logger.warn("Someone attempted to register a different logger, but it's locked!"); } }
	
	public static void registerMapper(ObjectMapper map) { if(Mapper.mapper == null) { Mapper.mapper = map; } else { Log.logger.warn("Someone attempted to register a different logger, but it's locked!"); } }
	
	@Getter private static final List<ClassLoader> registeredClassLoaders = new ArrayList<ClassLoader>();
	public static void registerClassLoader(ClassLoader cl) { registeredClassLoaders.add(cl); }
	
	private static final Map<String, Map<Integer, String>> registeredIntegerStringMaps = new HashMap<>();
	public static Map<String, Map<Integer, String>> getRegisteredIntegerStringMaps() { return Collections.unmodifiableMap(registeredIntegerStringMaps); }
	public static void registerIntStringMap(String mapID, Map<Integer, String> map)
	{
		if(registeredIntegerStringMaps.containsKey(mapID)) Log.getLogger().warn("A map with the ID " + mapID + " has already been registered! Overriding...");
		registeredIntegerStringMaps.put(mapID, map);
	}
	
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
}
