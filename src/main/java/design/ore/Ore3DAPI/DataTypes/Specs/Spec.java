package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javafx.beans.property.Property;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(value = {
	@Type(value = BooleanSpec.class, name = "boolspec"),
	@Type(value = DoubleSpec.class, name = "doublespec"),
	@Type(value = StringSpec.class, name = "strspec"),
	@Type(value = PositiveIntSpec.class, name = "posintspec"),
	@Type(value = EnumSpec.class, name = "enumspec"),
	@Type(value = IntSpec.class, name = "intspec"),
	@Type(value = SearchableIntegerStringMapSpec.class, name = "sismspec"),
	@Type(value = IntegerStringMapSpec.class, name = "ismspec"),
})
@NoArgsConstructor
public abstract class Spec<T>
{	
	public Spec(String id, Property<T> value, boolean readOnly, String section)
	{
		this.id = id;
		this.property = value;
		this.readOnly = readOnly;
		this.section = section;
	}
	
	@Getter @Setter protected boolean readOnly;
	@Getter @Setter protected String section;
	@Getter protected Property<T> property;
	@Getter @Setter protected String id;

	public void setValue(T val) { if(!readOnly) property.setValue(val); }
	public T getValue() { return property.getValue(); }
	
	public abstract Pane getUI(List<Property<?>> props);
	
	@Override
	public boolean equals(Object o)
	{
		if(o == this || !(o instanceof Spec)) return false;
		
		Spec<?> compare = (Spec<?>) o;
		return this.id.equals(compare.id);
	}
}
