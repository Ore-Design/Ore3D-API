package design.ore.Ore3DAPI.DataTypes;

import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BuildTreeViewItem
{
	Build build;
	String displayName;
	
	public boolean isSelectable() { return build != null; }
}
