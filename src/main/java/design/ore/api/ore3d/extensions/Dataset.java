package design.ore.api.ore3d.extensions;

import java.util.List;
import java.util.Map;

import org.pf4j.ExtensionPoint;

import design.ore.api.ore3d.data.BuildTreeViewItem;
import design.ore.api.ore3d.data.interfaces.CustomBOMComponentUI;
import design.ore.api.ore3d.data.pricing.BOMEntry;
import design.ore.api.ore3d.data.pricing.RoutingEntry;
import design.ore.api.ore3d.data.wrappers.ProductFamily;
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
