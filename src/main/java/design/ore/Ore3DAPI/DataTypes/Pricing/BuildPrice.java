package design.ore.Ore3DAPI.DataTypes.Pricing;

import design.ore.Ore3DAPI.DataTypes.Records.Build;
import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.Getter;

public class BuildPrice
{
	private final Build parent;
	
	@Getter SimpleDoubleProperty unitPriceProperty;
	@Getter SimpleBooleanProperty overriddenProperty;
	
	public BuildPrice(Build parent, DoubleBinding unitPriceSourceBinding)
	{
		this.parent = parent;
		unitPriceProperty = new SimpleDoubleProperty();
		unitPriceProperty.bind(unitPriceSourceBinding);
		overriddenProperty = new SimpleBooleanProperty(false);
	}
	
	public void reAttachUnitSource(DoubleBinding unitSource) { unitPriceProperty.bind(unitSource); }
	
	public DoubleBinding total() { return unitPriceProperty.multiply(((PositiveIntSpec)parent.getQuantity()).getNumberProperty()); }
	
	public void reset() { overriddenProperty.setValue(false); }
	public void override(double price)
	{
		overriddenProperty.setValue(true);
		unitPriceProperty.setValue(price);
	}
}
