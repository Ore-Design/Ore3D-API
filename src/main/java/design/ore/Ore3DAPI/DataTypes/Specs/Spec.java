package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
	
	public Spec(String id, Property<T> value, boolean readOnly, String section, ObservableValue<T> bindTo)
	{
		this.id = id;
		this.property = value;
		this.readOnly = readOnly;
		this.section = section;
		property.bind(bindTo);
	}
	
	@Getter @Setter protected boolean readOnly;
	@Getter @Setter protected String section;
	protected Property<T> property;
	@Getter @Setter protected String id;
	
	List<ChangeListener<? super T>> listeners = new ArrayList<>();

	public void setValue(T val) { if(!readOnly) property.setValue(val); }
	public T getValue() { return property.getValue(); }
	public void addListener(ChangeListener<? super T> listener)
	{
		property.addListener(listener);
		listeners.add(listener);
	}
	public void clearListeners()
	{
		for(ChangeListener<? super T> listener : listeners) property.removeListener(listener);
		listeners.clear();
	}
	public void bind(ObservableValue<? extends T> obs) { if(!readOnly) property.bind(obs); }
	public void bindBidirectional(Property<T> other) { if(!readOnly) property.bindBidirectional(other); }
	
	public abstract Pane getUI(List<Spec<?>> props);
	
	@Override
	public boolean equals(Object o)
	{
		if(o == this || !(o instanceof Spec)) return false;
		
		Spec<?> compare = (Spec<?>) o;
		return this.id.equals(compare.id);
	}
}
