package design.ore.ore3dapi.data.specs;

import java.util.concurrent.Callable;

import design.ore.ore3dapi.data.core.Build;
import design.ore.ore3dapi.data.interfaces.ISpecUI;
import design.ore.ore3dapi.data.specs.ui.StringSpecUI;

public class StringSpec extends Spec<String>
{
	public StringSpec(Build parent, String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public StringSpec(Build parent, String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<String> calculateOnDirty)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null); }
	
	public StringSpec(Build parent, String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<String> calculateOnDirty, String uniqueBehaviorNotifier)
	{ super(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }

	@Override
	public ISpecUI<String> generateUI() { return new StringSpecUI(this); }
}
