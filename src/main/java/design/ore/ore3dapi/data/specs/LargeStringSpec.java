package design.ore.ore3dapi.data.specs;

import java.util.concurrent.Callable;

import design.ore.ore3dapi.data.core.Build;
import design.ore.ore3dapi.data.interfaces.ISpecUI;
import design.ore.ore3dapi.data.specs.ui.LargeStringSpecUI;

public class LargeStringSpec extends StringSpec
{
	public LargeStringSpec(Build parent, String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ super(parent, id, initialValue, readOnly, section, countsAsMatch); }
	
	public LargeStringSpec(Build parent, String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<String> calculateOnDirty)
	{ super(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty); }
	
	public LargeStringSpec(Build parent, String id, String initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<String> calculateOnDirty, String uniqueBehaviorNotifier)
	{ super(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }

	@Override
	public ISpecUI<String> generateUI() { return new LargeStringSpecUI(this); }
}
