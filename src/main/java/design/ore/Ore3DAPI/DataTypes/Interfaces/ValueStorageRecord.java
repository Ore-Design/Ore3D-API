package design.ore.Ore3DAPI.DataTypes.Interfaces;

import java.util.HashMap;
import java.util.Map;

import design.ore.Ore3DAPI.DataTypes.StoredValue;

public abstract class ValueStorageRecord
{
	private Map<String, StoredValue> values = new HashMap<String, StoredValue>();

	public StoredValue getStoredValue(String key) { return values.get(key); }
	public Map<String, StoredValue> getStoredValues() { return values; }
	public StoredValue putStoredValue(String key, StoredValue value) { return values.put(key, value); }
	public StoredValue putStoredValue(String key, String value, boolean userViewable, boolean userEditable) { return values.put(key, new StoredValue(value, userViewable, userEditable)); }
	public void putStoredValues(Map<String, StoredValue> values) { this.values.putAll(values); }
	public StoredValue removeStoredValue(String key) { return this.values.remove(key); }
}
