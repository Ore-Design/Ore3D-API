package design.ore.Ore3DAPI.DataTypes.Wrappers;

import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CatalogItem
{
	Build build;
	double price;
	String displayName;
	
	@Override
	public String toString() { return displayName + " (Build Type: " + build.getClass() + ") $" + price; }
}
