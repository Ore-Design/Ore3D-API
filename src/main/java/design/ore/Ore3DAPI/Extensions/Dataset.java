package design.ore.Ore3DAPI.Extensions;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.pf4j.ExtensionPoint;

import com.fasterxml.jackson.databind.jsontype.NamedType;

import design.ore.Ore3DAPI.DataTypes.Build.Build;
import design.ore.Ore3DAPI.DataTypes.CRM.Transaction;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Wrappers.CatalogItem;
import design.ore.Ore3DAPI.DataTypes.Wrappers.ProductFamily;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public interface Dataset extends ExtensionPoint
{
	List<ProductFamily> retrieveProductFamilies();
	Map<String, ? extends BOMEntry> retrieveBOM();
	Map<String, ? extends RoutingEntry> retrieveRoutings();
	List<NamedType> retrieveBuildSubtypes();
	Map<String, Build> retrieveChildBuilds();
	void reload();
	List<CatalogItem> retrieveCatalog();
	Map<Image, Function<Transaction, Pane>> retrieveCustomEditButtons();
}
