package design.ore.Ore3DAPI.data.interfaces;

import design.ore.Ore3DAPI.data.pricing.BOMEntry;
import javafx.scene.layout.Pane;

public interface CustomBOMComponentUI
{
	public Pane getUI();
	public BOMEntry getGeneratedBOMEntry();
}
