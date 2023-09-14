package design.ore.Ore3DAPI.Extensions;

import java.util.List;
import java.util.Map;

import org.pf4j.ExtensionPoint;

import design.ore.Ore3DAPI.DataTypes.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.ProductFamily;

public interface Dataset extends ExtensionPoint
{
	List<ProductFamily> retrieveCatalog();
	Map<Integer, ? extends BOMEntry> retrieveBOM();
}
