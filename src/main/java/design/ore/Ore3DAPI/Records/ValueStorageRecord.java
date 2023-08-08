package design.ore.Ore3DAPI.Records;

import java.util.HashMap;
import java.util.Map;

import design.ore.Ore3DAPI.Records.Subtypes.Spec;

public class ValueStorageRecord
{
	private Map<String, Spec> values = new HashMap<String, Spec>();

	public Spec getStoredValue(String key) { return values.get(key); }
	public Spec putStoredValue(String key, Spec value) { return values.put(key, value); }
	public void updateStoredValue(String key, Object value) { values.get(key).setValue(value); }
}
