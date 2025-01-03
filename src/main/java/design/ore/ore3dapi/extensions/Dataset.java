package design.ore.ore3dapi.extensions;

import java.util.List;
import java.util.Map;

import org.pf4j.ExtensionPoint;

import design.ore.ore3dapi.data.BuildTreeViewItem;
import design.ore.ore3dapi.data.interfaces.CustomBOMComponentUI;
import design.ore.ore3dapi.data.pricing.BOMEntry;
import design.ore.ore3dapi.data.pricing.RoutingEntry;
import design.ore.ore3dapi.data.wrappers.ProductFamily;
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
