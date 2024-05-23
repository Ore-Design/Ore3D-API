package design.ore.Ore3DAPI.DataTypes.Interfaces;

import design.ore.Ore3DAPI.DataTypes.Pricing.BOMEntry;
import javafx.scene.layout.Pane;

public interface CustomBOMComponentUI
{
	public Pane getUI();
	public BOMEntry getGeneratedBOMEntry();
}
