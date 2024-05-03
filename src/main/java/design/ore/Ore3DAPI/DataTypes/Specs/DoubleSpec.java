package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.concurrent.Callable;

import design.ore.Ore3DAPI.DataTypes.Interfaces.ISpecUI;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.DataTypes.Specs.UI.DoubleSpecUI;

public class DoubleSpec extends NumberSpec<Double>
{	
	public DoubleSpec(Build parent, String id, double initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public DoubleSpec(Build parent, String id, double initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null); }
	
	public DoubleSpec(Build parent, String id, double initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Number> calculateOnDirty, String uniqueBehaviorNotifier)
	{ super(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }

	@Override
	public ISpecUI<Number> generateUI() { return new DoubleSpecUI(this); }

	@Override
	public int intValue() { return get().intValue(); }

	@Override
	public long longValue() { return get().longValue(); }

	@Override
	public float floatValue() { return get().floatValue(); }

	@Override
	public double doubleValue() { return get().doubleValue(); }
	
	@Override
	public Double getValue() { return get().doubleValue(); }
}
