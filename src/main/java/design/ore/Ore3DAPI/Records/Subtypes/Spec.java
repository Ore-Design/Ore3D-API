package design.ore.Ore3DAPI.Records.Subtypes;

public class Spec
{
	public Spec(ValueType type)
	{
		this.type = type;
	}
	
	String id;
	Object value;
	ValueType type;
	
	public Object getValue() { return value; }
	public void setValue(Object obj)
	{
		if(obj.getClass().equals(type.getType())) value = obj;
		else System.err.println("Attempted to set spec " + id + " to " + obj.getClass().toString() + " even though type of spec is " + type.getType().toString());
	}
}
