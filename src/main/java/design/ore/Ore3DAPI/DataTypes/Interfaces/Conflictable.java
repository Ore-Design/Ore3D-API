package design.ore.Ore3DAPI.DataTypes.Interfaces;

import java.util.List;

import design.ore.Ore3DAPI.DataTypes.Conflict;

public interface Conflictable
{
	public List<Conflict> getConflicts();
	public void addConflict(Conflict conflict);
	public void clearConflicts();
}
