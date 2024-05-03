package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.concurrent.Callable;

import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import javafx.beans.value.ObservableNumberValue;

public abstract class NumberSpec<T extends Number> extends Spec<Number> implements ObservableNumberValue
{
	public NumberSpec(Build parent, String id, T initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public NumberSpec(Build parent, String id, T initialValue, boolean readOnly, String section, boolean countsAsMatch, boolean positiveOnly)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null, positiveOnly); }
	
	public NumberSpec(Build parent, String id, T initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null); }
	
	public NumberSpec(Build parent, String id, T initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty, boolean positiveOnly)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null, positiveOnly); }
	
	public NumberSpec(Build parent, String id, T initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty, String uniqueBehaviorNotifier)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier, false); }
	
	public NumberSpec(Build parent, String id, T initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty, String uniqueBehaviorNotifier, boolean positiveOnly)
	{ super(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }
}
