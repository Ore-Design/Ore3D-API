package design.ore.ore3dapi.data.interfaces;

import design.ore.ore3dapi.data.pricing.BOMEntry;
import javafx.scene.layout.Pane;

public interface CustomBOMComponentUI
{
	public Pane getUI();
	public BOMEntry getGeneratedBOMEntry();
}
