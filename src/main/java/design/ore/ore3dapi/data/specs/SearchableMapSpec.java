package design.ore.ore3dapi.data.specs;

import java.util.Map;
import java.util.concurrent.Callable;

import design.ore.ore3dapi.data.core.Build;
import design.ore.ore3dapi.data.interfaces.ISpecUI;
import design.ore.ore3dapi.data.specs.ui.SearchableMapSpecUI;
import javafx.beans.property.SimpleObjectProperty;

public class SearchableMapSpec<T, V> extends MapSpec<T, V>
{
	public SearchableMapSpec(Build parent, String id, SimpleObjectProperty<Map<T, V>> map, T initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ super(parent, id, map, initialValue, readOnly, section, countsAsMatch); }
	
	public SearchableMapSpec(Build parent, String id, SimpleObjectProperty<Map<T, V>> map, T initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<T> calculateOnDirty)
	{ super(parent, id, map, initialValue, readOnly, section, countsAsMatch, calculateOnDirty); }
	
	public SearchableMapSpec(Build parent, String id, SimpleObjectProperty<Map<T, V>> map, T initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<T> calculateOnDirty, String uniqueBehaviorNotifier)
	{ super(parent, id, map, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier); }

	@Override
	public ISpecUI<T> generateUI() { return new SearchableMapSpecUI<T, V>(this); }
}
