package design.ore.Ore3DAPI.Records.Subtypes.Pricing;

import design.ore.Ore3DAPI.Records.Build;
import lombok.Getter;
import lombok.Setter;

public class BuildPrice
{
	private final Build parent;
	
	@Getter @Setter double unitPrice;
	@Getter boolean overridden;
	double overriddenPrice;
	
	public BuildPrice(Build parent)
	{
		this.parent = parent;
		unitPrice = 0;
		overridden = false;
		overriddenPrice = 0;
	}
	
	public double total() { return overridden ? overriddenPrice : unitPrice * ((double) parent.getQuantityProperty().get()); }
	
	public void reset() { overridden = false; }
	public void override(double price)
	{
		overridden = true;
		overriddenPrice = price;
	}
}
