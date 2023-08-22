package design.ore.Ore3DAPI.Records.Subtypes.Specs;

import javafx.beans.property.Property;
import javafx.scene.layout.Pane;
import lombok.Getter;

public abstract class Spec<T>
{
	public Spec(String id, Property<T> value, boolean readOnly, String section)
	{
		this.id = id;
		this.value = value;
		this.readOnly = readOnly;
		this.section = section;
	}
	
	@Getter protected final boolean readOnly;
	@Getter protected final String section;
	protected final Property<T> value;
	protected final String id;
	
	public void setValue(T val) { if(!readOnly) value.setValue(val); }
	public T getValue() { return value.getValue(); }
	
	public abstract Pane getUI();
	
	@Override
	public boolean equals(Object o)
	{
		if(o == this || !(o instanceof Spec)) return false;
		
		Spec<?> compare = (Spec<?>) o;
		return this.id.equals(compare.id);
	}
}
