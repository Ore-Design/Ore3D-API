package design.ore.api.ore3d.data;

import design.ore.api.ore3d.data.core.Build;
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
