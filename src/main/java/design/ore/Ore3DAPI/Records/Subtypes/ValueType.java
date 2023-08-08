package design.ore.Ore3DAPI.Records.Subtypes;

public class ValueType
{
	public static final ValueType INTEGER = new ValueType(Integer.class);
	public static final ValueType DOUBLE = new ValueType(Double.class);
	public static final ValueType STRING = new ValueType(String.class);
	public static final ValueType LONG = new ValueType(Long.class);
	public static final ValueType FLOAT = new ValueType(Float.class);
	public static final ValueType CHARACTER = new ValueType(Character.class);
	
	public ValueType(Class<?> type) { this.type = type; }
	
	final Class<?> type;
	
	public boolean isValidValue(Object object) { return object.getClass().equals(type); }
	public Class<?> getType() { return type; }
}
