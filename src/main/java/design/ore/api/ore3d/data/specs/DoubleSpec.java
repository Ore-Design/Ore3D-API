package design.ore.api.ore3d.data.specs;

import java.util.concurrent.Callable;

import design.ore.api.ore3d.data.core.Build;
import design.ore.api.ore3d.data.interfaces.ISpecUI;
import design.ore.api.ore3d.data.specs.ui.DoubleSpecUI;

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
	public int intValue() { return get() == null ? null : get().intValue(); }

	@Override
	public long longValue() { return get() == null ? null : get().longValue(); }

	@Override
	public float floatValue() { return get() == null ? null : get().floatValue(); }

	@Override
	public double doubleValue() { return get() == null ? null : get().doubleValue(); }
	
	@Override
	public Double getValue() { return get() == null ? null : get().doubleValue(); }
}
