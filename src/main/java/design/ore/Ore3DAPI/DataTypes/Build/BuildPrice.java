package design.ore.Ore3DAPI.DataTypes.Build;

import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import javafx.beans.binding.Bindings;
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
		
		overriddenUnitPriceProperty = new SimpleDoubleProperty(-Double.MAX_VALUE);
		unitPriceOverriddenProperty = overriddenUnitPriceProperty.greaterThan(-Double.MAX_VALUE);
		
		rebindPricing(parent);
	}
	
	public void rebindPricing(Build parent)
	{
		unitPriceProperty.bind(Bindings.when(unitPriceOverriddenProperty)
			.then(overriddenUnitPriceProperty).otherwise(parent.getUnitPrice()));
		totalPrice = (DoubleBinding) Bindings.when(unitPriceOverriddenProperty)
			.then(overriddenUnitPriceProperty.multiply(((PositiveIntSpec) parent.getQuantity()).getIntProperty()))
			.otherwise(parent.getTotalPrice());
	}
	
	public void reset() { overriddenUnitPriceProperty.setValue(-Double.MAX_VALUE); }
	public void override(double price)
	{
		overriddenUnitPriceProperty.setValue(price);
	}
}
