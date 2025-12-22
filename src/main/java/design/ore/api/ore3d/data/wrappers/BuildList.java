package design.ore.api.ore3d.data.wrappers;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import design.ore.api.ore3d.data.core.Build;
import design.ore.api.ore3d.jackson.ObservableListSerialization;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@JsonDeserialize(using = ObservableListSerialization.BuildList.Deserializer.class)
@JsonSerialize(using = ObservableListSerialization.BuildList.Serializer.class)
public class BuildList implements ObservableList<Build>
{
	ObservableList<Build> list;
	
	public BuildList()
	{
		list = FXCollections.observableArrayList();
	}
	public BuildList(List<Build> builds)
	{
		list = FXCollections.observableArrayList(builds);
	}
	
	@Override
	public int size() { return list.size(); }

	@Override
	public boolean isEmpty() { return list.isEmpty(); }

	@Override
	public boolean contains(Object o) { return list.contains(o); }

	@Override
	public Iterator<Build> iterator() { return list.iterator(); }

	@Override
	public Object[] toArray() { return list.toArray(); }

	@Override
	public <T> T[] toArray(T[] a) { return list.toArray(a); }

	@Override
	public boolean add(Build e) { return list.add(e); }

	@Override
	public boolean remove(Object o) { return list.remove(o); }

	@Override
	public boolean containsAll(Collection<?> c) { return list.containsAll(c); }

	@Override
	public boolean addAll(Collection<? extends Build> c) { return list.addAll(c); }

	@Override
	public boolean addAll(int index, Collection<? extends Build> c) { return list.addAll(c); }

	@Override
	public boolean removeAll(Collection<?> c) { return list.removeAll(c); }

	@Override
	public boolean retainAll(Collection<?> c) { return list.retainAll(c); }

	@Override
	public void clear() { list.clear(); }

	@Override
	public Build get(int index) { return list.get(index); }

	public Build getByUID(int buildUUID)
	{
		try{ return list.stream().filter(b -> b.getBuildUUID() == buildUUID).findFirst().get(); }
		catch(Exception e) { return null; }
	}

	@Override
	public Build set(int index, Build element) { return list.set(index, element); }

	@Override
	public void add(int index, Build element) { list.add(index, element); }

	@Override
	public Build remove(int index) { return list.remove(index); }

	@Override
	public int indexOf(Object o) { return list.indexOf(o); }

	@Override
	public int lastIndexOf(Object o) { return list.lastIndexOf(o); }

	@Override
	public ListIterator<Build> listIterator() { return list.listIterator(); }

	@Override
	public ListIterator<Build> listIterator(int index) { return list.listIterator(index); }

	@Override
	public List<Build> subList(int fromIndex, int toIndex) { return list.subList(fromIndex, toIndex); }

	@Override
	public void addListener(InvalidationListener listener) { list.addListener(listener); }

	@Override
	public void removeListener(InvalidationListener listener) { list.removeListener(listener); }

	@Override
	public void addListener(ListChangeListener<? super Build> listener) { list.addListener(listener); }

	@Override
	public void removeListener(ListChangeListener<? super Build> listener) { list.removeListener(listener); }

	@Override
	public boolean addAll(Build... elements) { return list.addAll(elements); }

	@Override
	public boolean setAll(Build... elements) { return list.setAll(elements); }

	@Override
	public boolean setAll(Collection<? extends Build> col) { return list.setAll(col); }

	@Override
	public boolean removeAll(Build... elements) { return list.removeAll(elements); }

	@Override
	public boolean retainAll(Build... elements) { return list.retainAll(elements); }

	@Override
	public void remove(int from, int to) { list.remove(from, to); }

}
