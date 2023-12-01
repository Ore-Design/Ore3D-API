package design.ore.Ore3DAPI.DataTypes.Wrappers;

import design.ore.Ore3DAPI.DataTypes.Build.Build;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CatalogItem
{
	Build build;
	double price;
}
