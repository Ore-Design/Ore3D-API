package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
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
	@Type(value = LinkedDoubleSpec.class, name = "ldspec"),
	@Type(value = LargeTextSpec.class, name = "ltspec"),
})

public abstract class Spec<T>
{
	public Spec() { valueProperty = new SimpleObjectProperty<T>(); }
	
	public Spec(Build parent, String id, Property<T> value, ObservableBooleanValue readOnly, String section, boolean countsAsMatch, Callable<T> calculateOnDirty, String uniqueBehaviorNotifier)
	{
		this.id = id;
		this.valueProperty = value;
		this.valueProperty.addListener((obs, oldVal, newVal) -> runListeners(obs, oldVal, newVal));
		this.readOnlyProperty.bind(readOnly);
		this.section = section;
		this.countsAsMatch = countsAsMatch;
		this.calculateOnDirty = calculateOnDirty;
		this.parent = parent;
		this.uniqueBehaviorNotifierProperty.setValue(uniqueBehaviorNotifier);
	}

	@Getter protected final SimpleBooleanProperty readOnlyProperty = new SimpleBooleanProperty(false);
	@Setter protected boolean countsAsMatch;
	public boolean countsAsMatch() { return countsAsMatch; }
	@Getter @Setter protected String section;
	@Getter @Setter protected String id;
	@Getter protected final Property<T> valueProperty; // TODO: Refactor for read only
	@JsonIgnore @Getter @Setter protected Callable<T> calculateOnDirty;
	@JsonIgnore List<ChangeListener<? super T>> listeners = new ArrayList<>();
	@JsonIgnore protected Build parent;
	@JsonIgnore @Getter protected final SimpleStringProperty uniqueBehaviorNotifierProperty = new SimpleStringProperty();
	@JsonIgnore @Getter protected final SimpleBooleanProperty holdCalculateTillCompleteProperty = new SimpleBooleanProperty(false);

	public void setValue(T val) { valueProperty.setValue(val); }
	public T getValue() { return valueProperty.getValue(); }
	public void addListener(ChangeListener<? super T> listener) { listeners.add(listener); }
	public void removeListener(ChangeListener<? super T> listener) { listeners.remove(listener); }
	public void clearListeners() { listeners.clear(); }
	protected void runListeners(ObservableValue<? extends T> obs, T oldVal, T newVal) { for(ChangeListener<? super T> listener : listeners) { listener.changed(obs, oldVal, newVal); } }
	public void bind(ObservableValue<? extends T> obs) { valueProperty.bind(obs); }
	public void bindBidirectional(Property<T> other) { valueProperty.bindBidirectional(other); }
	public boolean isReadOnly() { return readOnlyProperty.get(); }
	
	public void setPropertyToCallable()
	{
		if(calculateOnDirty != null)
		{
			try
			{
				T calledValue = calculateOnDirty.call();
				if(calledValue != null) valueProperty.setValue(calledValue);
			}
			catch (Exception e) { Util.Log.getLogger().warn("Error assigning value from Callable to property!"); Log.getLogger().debug(Util.stackTraceArrayToString(e)); }
		}
	}
	
	public abstract Pane getUI(List<Spec<?>> props, String popoutID);
	
	public boolean matches(Spec<?> spec)
	{
		if(spec == this) return true;
		
		return this.id.equals(spec.id) && this.getValue().equals(spec.getValue());
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == this || !(o instanceof Spec)) return false;
		
		Spec<?> compare = (Spec<?>) o;
		return this.id.equals(compare.id);
	}
}
