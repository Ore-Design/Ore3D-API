package design.ore.api.ore3d.data.interfaces;

import design.ore.api.ore3d.data.pricing.BOMEntry;
import javafx.scene.layout.Pane;

public interface CustomBOMComponentUI
{
	public Pane getUI();
	public BOMEntry getGeneratedBOMEntry();
}
