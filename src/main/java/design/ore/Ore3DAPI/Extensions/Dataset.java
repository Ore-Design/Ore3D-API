package design.ore.Ore3DAPI.Extensions;

import java.util.List;
import java.util.Map;

import org.pf4j.ExtensionPoint;

import design.ore.Ore3DAPI.DataTypes.BuildTreeViewItem;
import design.ore.Ore3DAPI.DataTypes.Interfaces.CustomBOMComponentUI;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Wrappers.ProductFamily;
import javafx.scene.control.TreeItem;

public interface Dataset extends ExtensionPoint
{
	List<ProductFamily> retrieveProductFamilies();
	TreeItem<BuildTreeViewItem> retrieveNewBuildListViewTree();
	Map<String, ? extends BOMEntry> retrieveBOM();
	Map<String, ? extends RoutingEntry> retrieveRoutings();
	void reload();
	CustomBOMComponentUI getCreateBOMComponentUI();
}
