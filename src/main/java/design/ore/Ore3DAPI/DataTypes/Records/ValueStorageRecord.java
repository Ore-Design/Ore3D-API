package design.ore.Ore3DAPI.DataTypes.Records;

import java.util.HashMap;
import java.util.Map;

public abstract class ValueStorageRecord
{
	private Map<String, String> values = new HashMap<String, String>();

	public String getStoredValue(String key) { return values.get(key); }
	public String putStoredValue(String key, String value) { return values.put(key, value); }
	public void putStoredValues(Map<String, String> values) { values.putAll(values); }
}
