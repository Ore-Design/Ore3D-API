package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.Ore3DAPI.DataTypes.Interfaces.ISpecUI;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.DataTypes.Specs.UI.MapSpecUI;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;

public class MapSpec<T, V> extends Spec<T>
{
	public MapSpec(Build parent, String id, SimpleObjectProperty<Map<T, V>> map, T initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, map, initialValue, readOnly, section, countsAsMatch, null); }
	
	public MapSpec(Build parent, String id, SimpleObjectProperty<Map<T, V>> map, T initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<T> calculateOnDirty)
	{ this(parent, id, map, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null); }
	
	public MapSpec(Build parent, String id, SimpleObjectProperty<Map<T, V>> map, T initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<T> calculateOnDirty, String uniqueBehaviorNotifier)
	{
		super(parent);
		this.map.bind(map);
		this.id = id;
		
		if(map.getValue() != null && map.getValue().size() > 0)
		{
			if(map.getValue().containsKey(initialValue) || initialValue == null) setValue(initialValue);
			else setValue(map.getValue().keySet().iterator().next());
		}
		
		this.countsAsMatchProperty.set(countsAsMatch);
		this.readOnlyProperty.set(readOnly);
		this.section = section;
		this.calculateOnDirty = calculateOnDirty;
		this.uniqueBehaviorNotifierProperty.setValue(uniqueBehaviorNotifier);
		
		addListener((obs, oldVal, newVal) -> linkedSpecs.forEach(s -> { if(s.linkIsActive()) s.setValue(newVal); }));
	}
	
	@Getter @JsonIgnore SimpleObjectProperty<Predicate<T>> filterProperty = new SimpleObjectProperty<>();
	@JsonIgnore public Predicate<T> getFilter() { return filterProperty.get(); }
	@JsonIgnore public void setFilter(Predicate<T> filter) { filterProperty.set(filter); }

	@JsonIgnore @Getter final SimpleObjectProperty<Map<T, V>> map = new SimpleObjectProperty<>();
	@JsonIgnore public String getStringValue()
	{
		if(getValue() != null && map.getValue() != null) return map.getValue().get(getValue()).toString();
		return "";
	}
	
	// Custom implementation of 'set()' to handle invalid entries per the set map.
	@Override
	public void set(T val)
	{
		if(map.getValue() == null || map.getValue().size() <= 0) { /*Log.getLogger().warn("Map has not yet been initialized! Wait to initialize map before setting a value!");*/ }
		else if(map.getValue().containsKey(val) || val == null) super.set(val);
		else throw new IllegalArgumentException("Map for spec " + id + " does not contain key " + val + "! Options are " + map.getValue().keySet() + ".");
	}

	@Override
	public ISpecUI<T> generateUI() { return new MapSpecUI<T, V>(this); }
}
