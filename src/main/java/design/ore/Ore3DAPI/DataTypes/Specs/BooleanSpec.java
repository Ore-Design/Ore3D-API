package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.concurrent.Callable;

import design.ore.Ore3DAPI.DataTypes.Interfaces.ISpecUI;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.DataTypes.Specs.UI.BooleanSpecUI;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;

public class BooleanSpec extends Spec<Boolean>
{
	public BooleanSpec(Build parent, String id, boolean initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public BooleanSpec(Build parent, String id, boolean initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Boolean> calculateOnDirty)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null); }
	
	public BooleanSpec(Build parent, String id, boolean initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<Boolean> calculateOnDirty, String uniqueBehaviorNotifier)
	{ super(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }

	@Override
	public ISpecUI<Boolean> generateUI() { return new BooleanSpecUI(this); }
	
    public BooleanBinding and(final ObservableBooleanValue other) { return Bindings.createBooleanBinding(() -> getValue() && other.getValue(), this, other); }

    public BooleanBinding or(final ObservableBooleanValue other) { return Bindings.createBooleanBinding(() -> getValue() || other.getValue(), this, other); }

    public BooleanBinding not() { return Bindings.createBooleanBinding(() -> !getValue(), this); }

    public BooleanBinding isEqualTo(final ObservableBooleanValue other) { return Bindings.equal(this, other); }

    public BooleanBinding isNotEqualTo(final ObservableBooleanValue other) { return Bindings.notEqual(this, other); }
}
