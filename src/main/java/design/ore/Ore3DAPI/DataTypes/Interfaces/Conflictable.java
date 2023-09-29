package design.ore.Ore3DAPI.DataTypes.Interfaces;

import design.ore.Ore3DAPI.DataTypes.Conflict;
import javafx.collections.ObservableList;

public interface Conflictable
{
	public ObservableList<Conflict> getConflicts();
	public void addConflict(Conflict conflict);
	public void clearConflicts();
}
