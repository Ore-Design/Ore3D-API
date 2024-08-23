package design.ore.Ore3DAPI.data.specs;

import java.util.concurrent.Callable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.Ore3DAPI.data.core.Build;
import design.ore.Ore3DAPI.data.interfaces.ISpecUI;
import design.ore.Ore3DAPI.data.specs.ui.IntegerSpecUI;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;

public class IntegerSpec extends NumberSpec<Integer>
{
	public IntegerSpec(Build parent, String id, int initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public IntegerSpec(Build parent, String id, int initialValue, boolean readOnly, String section, boolean countsAsMatch, boolean positiveOnly)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null, positiveOnly); }
	
	public IntegerSpec(Build parent, String id, int initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null); }
	
	public IntegerSpec(Build parent, String id, int initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty, boolean positiveOnly)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null, positiveOnly); }
	
	public IntegerSpec(Build parent, String id, int initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty, String uniqueBehaviorNotifier)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier, false); }
	
	public IntegerSpec(Build parent, String id, int initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty, String uniqueBehaviorNotifier, boolean positiveOnly)
	{
		super(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier);
		positiveOnlyProperty.set(positiveOnly);
	}
	
	@JsonIgnore @Getter private SimpleBooleanProperty positiveOnlyProperty = new SimpleBooleanProperty(false);
	public boolean isPositiveOnly() { return positiveOnlyProperty.get(); }
	public void setPositiveOnly(boolean positiveOnly) { positiveOnlyProperty.set(positiveOnly); }

	@Override
	public ISpecUI<Number> generateUI() { return new IntegerSpecUI(this); }

	@Override
	public int intValue() { return get().intValue(); }

	@Override
	public long longValue() { return get().longValue(); }

	@Override
	public float floatValue() { return get().floatValue(); }

	@Override
	public double doubleValue() { return get().doubleValue(); }
	
	@Override
	public Integer getValue() { return get().intValue(); }
}
