package design.ore.Ore3DAPI.DataTypes.Records;

import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.Getter;

public class BuildPrice
{
	ReadOnlyDoubleWrapper unitPriceProperty;
	public ReadOnlyDoubleProperty getUnitPrice() { return unitPriceProperty.getReadOnlyProperty(); }
	SimpleDoubleProperty overriddenUnitPriceProperty;
	@Getter BooleanBinding unitPriceOverriddenProperty;
	@Getter DoubleBinding totalPrice;
	
	public BuildPrice(Build parent)
	{
		unitPriceProperty = new ReadOnlyDoubleWrapper();
		unitPriceProperty.bind(parent.getUnitPrice());
		
		overriddenUnitPriceProperty = new SimpleDoubleProperty(-1.0);
		unitPriceOverriddenProperty = overriddenUnitPriceProperty.greaterThanOrEqualTo(0.0);
		
		rebindPricing(parent);
		
		unitPriceOverriddenProperty.addListener((observable, oldVal, newVal) -> rebindPricing(parent));
	}
	
	public void rebindPricing(Build parent)
	{
		rebindTotalPrice(parent);
		rebindUnitPrice(parent);
	}
	
	private void rebindUnitPrice(Build parent)
	{
		if(unitPriceOverriddenProperty.get()) unitPriceProperty.bind(overriddenUnitPriceProperty);
		else unitPriceProperty.bind(parent.getUnitPrice());
	}
	
	private void rebindTotalPrice(Build parent)
	{
		if(unitPriceOverriddenProperty.get()) totalPrice = overriddenUnitPriceProperty.multiply(((PositiveIntSpec) parent.getQuantity()).getIntProperty());
		else totalPrice = parent.getTotalPrice();
	}
	
	public void reset() { overriddenUnitPriceProperty.setValue(-1.0); }
	public void override(double price)
	{
		unitPriceProperty.setValue(price);
	}
}
