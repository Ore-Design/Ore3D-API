package design.ore.api.ore3d.data.wrappers;

import design.ore.api.ore3d.data.core.Build;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CatalogItem
{
	String id;
	Build build;
	double price;
	String displayName;
	
	@Override
	public String toString() { return displayName + " (Build Type: " + build.getClass() + ") $" + price; }
}
