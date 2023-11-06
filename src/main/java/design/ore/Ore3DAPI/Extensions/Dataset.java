package design.ore.Ore3DAPI.Extensions;

import java.util.List;
import java.util.Map;

import org.pf4j.ExtensionPoint;

import com.fasterxml.jackson.databind.jsontype.NamedType;

import design.ore.Ore3DAPI.DataTypes.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.ProductFamily;
import design.ore.Ore3DAPI.DataTypes.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Records.Build;
import javafx.util.Pair;

public interface Dataset extends ExtensionPoint
{
	List<ProductFamily> retrieveCatalog();
	Map<String, ? extends BOMEntry> retrieveBOM();
	Map<String, ? extends RoutingEntry> retrieveRoutings();
	List<Pair<String, Map<Integer, String>>> retrieveIntegerStringMaps();
	List<NamedType> retrieveBuildSubtypes();
	Map<String, Build> retrieveChildBuilds();
	void reload();
}
