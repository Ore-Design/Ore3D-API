package design.ore.Ore3DAPI.DataTypes.Pricing;

import design.ore.Ore3DAPI.DataTypes.Records.Build;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import lombok.Getter;

public class BuildPrice
{
	ReadOnlyDoubleWrapper totalPriceProperty;
	public ReadOnlyDoubleProperty getTotal() { return totalPriceProperty.getReadOnlyProperty(); }
	SimpleDoubleProperty overriddenPriceProperty;
	@Getter BooleanBinding overriddenProperty;
	
	public BuildPrice(Build parent)
	{
		totalPriceProperty = new ReadOnlyDoubleWrapper();
		totalPriceProperty.bind(parent.getTotalPrice());
		
		overriddenPriceProperty = new SimpleDoubleProperty(-1.0);
		overriddenProperty = overriddenPriceProperty.greaterThanOrEqualTo(0.0);

		parent.getBom().addListener((ListChangeListener.Change<?> c) -> rebind(parent.getTotalPrice()));
		parent.getRoutings().addListener((ListChangeListener.Change<?> c) -> rebind(parent.getTotalPrice()));
		overriddenProperty.addListener((observable, oldVal, newVal) -> rebind(parent.getTotalPrice()));
	}
	
	private void rebind(DoubleBinding newBind)
	{
		if(overriddenProperty.get()) totalPriceProperty.bind(overriddenPriceProperty);
		else totalPriceProperty.bind(newBind);
	}
	
	public void reset() { overriddenPriceProperty.setValue(-1.0); }
	public void override(double price)
	{
		totalPriceProperty.setValue(price);
	}
}
