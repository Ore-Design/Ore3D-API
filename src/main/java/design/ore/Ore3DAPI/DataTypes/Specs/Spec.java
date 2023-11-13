package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import design.ore.Ore3DAPI.Util;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;
import lombok.Getter;
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
public abstract class Spec<T>
{
	public Spec()
	{
		this.calculateOnDirty = null;
	}
	
	public Spec(String id, Property<T> value, boolean readOnly, String section)
	{
		this.id = id;
		this.property = value;
		this.property.addListener((obs, oldVal, newVal) -> runListeners(obs, oldVal, newVal));
		this.readOnly = readOnly;
		this.section = section;
		this.calculateOnDirty = null;
	}
	
	public Spec(String id, Property<T> value, boolean readOnly, String section, Callable<T> calculateOnDirty)
	{
		this.id = id;
		this.property = value;
		this.property.addListener((obs, oldVal, newVal) -> runListeners(obs, oldVal, newVal));
		this.readOnly = readOnly;
		this.section = section;
		this.calculateOnDirty = calculateOnDirty;
	}

	@Getter @Setter protected boolean readOnly;
	@JsonMerge @Getter @Setter protected String section;
	protected Property<T> property;
	@Getter @Setter protected String id;
	@JsonIgnore @Getter protected final Callable<T> calculateOnDirty;
	@JsonIgnore List<ChangeListener<? super T>> listeners = new ArrayList<>();

	public void setValue(T val) { if(!readOnly) property.setValue(val); }
	public T getValue() { return property.getValue(); }
	public void addListener(ChangeListener<? super T> listener) { listeners.add(listener); }
	public void clearListeners() { listeners.clear(); }
	private void runListeners(ObservableValue<? extends T> obs, T oldVal, T newVal) { for(ChangeListener<? super T> listener : listeners) { listener.changed(obs, oldVal, newVal); } }
	public void bind(ObservableValue<? extends T> obs) { if(!readOnly) property.bind(obs); }
	public void bindBidirectional(Property<T> other) { if(!readOnly) property.bindBidirectional(other); }
	
	public void setPropertyToCallable()
	{
		if(calculateOnDirty != null)
		{
			try
			{
				T var = calculateOnDirty.call();
				property.setValue(var);
			}
			catch (Exception e) { Util.warnLog("Error assigning value from Callable to property!\n" + Util.stackTraceArrayToString(e)); }
		}
	}
	
	public abstract Pane getUI(List<Spec<?>> props);
	
	@Override
	public boolean equals(Object o)
	{
		if(o == this || !(o instanceof Spec)) return false;
		
		Spec<?> compare = (Spec<?>) o;
		return this.id.equals(compare.id);
	}
}
