package design.ore.Ore3DAPI.data.interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.JsonNode;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.Util.Mapper;
import design.ore.Ore3DAPI.data.StoredValue;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class ValueStorageRecord
{
	@JsonIgnore @Getter private Map<String, StoredValue> storedValues = new HashMap<String, StoredValue>();

	@JsonIgnore public StoredValue getStoredValue(String key) { return storedValues.get(key); }
	@JsonIgnore public StoredValue putStoredValue(String key, StoredValue value) { return storedValues.put(key, value); }
	@JsonIgnore public StoredValue putStoredValue(String key, String value, boolean userViewable, boolean userEditable) { return putStoredValue(key, new StoredValue(value, userViewable, userEditable)); }
	@JsonIgnore public void putStoredValues(Map<String, StoredValue> values) { this.storedValues.putAll(values); }
	@JsonIgnore public StoredValue removeStoredValue(String key) { return this.storedValues.remove(key); }
	
	@JsonIgnore
	public StoredValue putStoredValueIgnoringKeyCase(String key, StoredValue value)
	{
		removeValueFromKeyIgnoreKeyCase(key);
		return storedValues.put(key, value);
	}
	@JsonIgnore
	public StoredValue putStoredValueIgnoringKeyCase(String key, String value, boolean userViewable, boolean userEditable)
	{
		removeValueFromKeyIgnoreKeyCase(key);
		return putStoredValue(key, new StoredValue(value, userViewable, userEditable));
	}
	
	@JsonIgnore
	public void removeValueFromKeyIgnoreKeyCase(String key)
	{
		List<String> keysToRemove = new ArrayList<>();
		for(Entry<String, StoredValue> entry : storedValues.entrySet())
		{ if(entry.getKey().equalsIgnoreCase(key)) keysToRemove.add(entry.getKey()); }
		
		for(String str : keysToRemove) storedValues.remove(str);
	}
	
	@JsonProperty(value = "storedValuesWithRequiredData", access = Access.READ_ONLY)
	public Map<String, StoredValue> getStoredValuesWithRequiredData()
	{
		Map<String, StoredValue> values = new HashMap<>();
		for(Entry<String, StoredValue> entry : storedValues.entrySet())
		{
			if(entry.getValue().isDataRequired()) values.put(entry.getKey(), entry.getValue());
		}
		return values;
	}
	
	@JsonProperty(value = "storedValuesWithRequiredData", access = Access.WRITE_ONLY)
	public void setStoredValuesWithRequiredData(Map<String, StoredValue> values) { values.forEach((key, val) -> storedValues.put(key, val)); }
	
	@JsonAnySetter
	public void addAdditionalValue(String name, JsonNode value)
	{
		putStoredValue(name, value.toString(), false, false);
	}
	
	@JsonAnyGetter
	public Map<String, JsonNode> getAdditionalValues()
	{
		Map<String, JsonNode> values = new HashMap<>();
		for(Entry<String, StoredValue> entry : storedValues.entrySet())
		{
			if(entry.getValue().isDataRequired()) continue;
			
			if(entry.getKey() != null && !entry.getKey().equals("") && entry.getValue() != null && entry.getValue().getValue() != null && !entry.getValue().getValue().equals(""))
			{
				try { values.put(entry.getKey(), Mapper.getMapper().readValue(entry.getValue().getValue(), JsonNode.class)); }
				catch (Exception e)
				{
					try { values.put(entry.getKey(), Mapper.getMapper().readValue("\"" + entry.getValue().getValue() + "\"", JsonNode.class)); }
					catch (Exception ex) { Log.getLogger().warn(Util.formatThrowable("Error serializing stored value from class type '" + getClass().toString() + "'! Skipping!", e)); }
				}
			}
			else Log.getLogger().warn("Stored value with key " + entry.getKey() + " has null/empty data! Skipping...");
		}
		return values;
	}
}
