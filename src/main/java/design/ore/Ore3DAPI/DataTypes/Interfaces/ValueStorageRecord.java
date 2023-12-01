package design.ore.Ore3DAPI.DataTypes.Interfaces;

import java.util.HashMap;
import java.util.Map;

public abstract class ValueStorageRecord
{
	private Map<String, String> values = new HashMap<String, String>();

	public String getStoredValue(String key) { return values.get(key); }
	public Map<String, String> getStoredValues() { return values; }
	public String putStoredValue(String key, String value) { return values.put(key, value); }
	public void putStoredValues(Map<String, String> values) { this.values.putAll(values); }
	public String removeStoredValue(String key) { return this.values.remove(key); }
}
