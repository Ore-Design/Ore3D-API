package design.ore.Ore3DAPI.extensions;

import java.util.List;
import java.util.Map;

import org.pf4j.ExtensionPoint;

import design.ore.Ore3DAPI.data.BuildTreeViewItem;
import design.ore.Ore3DAPI.data.interfaces.CustomBOMComponentUI;
import design.ore.Ore3DAPI.data.pricing.BOMEntry;
import design.ore.Ore3DAPI.data.pricing.RoutingEntry;
import design.ore.Ore3DAPI.data.wrappers.ProductFamily;
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
