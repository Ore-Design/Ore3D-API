package design.ore.Ore3DAPI.DataTypes.Build;

import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import lombok.Getter;

public class BuildPrice
{
	ReadOnlyDoubleWrapper unitPriceProperty;
	public ReadOnlyDoubleProperty getUnitPrice() { return unitPriceProperty.getReadOnlyProperty(); }
	ReadOnlyDoubleWrapper overriddenUnitPriceProperty;
	public ReadOnlyDoubleProperty getOverriddenUnitPrice() { return overriddenUnitPriceProperty.getReadOnlyProperty(); }
	@Getter BooleanBinding unitPriceOverriddenProperty;

	ReadOnlyDoubleWrapper totalPriceProperty;
	public ReadOnlyDoubleProperty getTotalPrice() { return totalPriceProperty.getReadOnlyProperty(); }
	ReadOnlyDoubleWrapper overriddenTotalPriceProperty;
	public ReadOnlyDoubleProperty getOverriddenTotalPrice() { return overriddenTotalPriceProperty.getReadOnlyProperty(); }
	@Getter BooleanBinding totalPriceOverriddenProperty;
	@Getter NumberBinding unoverriddenTotalPriceBinding;
	
	public BuildPrice(Build parent)
	{
		unitPriceProperty = new ReadOnlyDoubleWrapper();
		overriddenUnitPriceProperty = new ReadOnlyDoubleWrapper(-Double.MAX_VALUE);
		
		totalPriceProperty = new ReadOnlyDoubleWrapper();
		overriddenTotalPriceProperty = new ReadOnlyDoubleWrapper(-Double.MAX_VALUE);
		
		unitPriceOverriddenProperty = overriddenUnitPriceProperty.greaterThan(-Double.MAX_VALUE);
		totalPriceOverriddenProperty = overriddenTotalPriceProperty.greaterThan(-Double.MAX_VALUE);
		
		if(parent == null) return;
		
		rebindPricing(parent);
	}
	
	public void rebindPricing(Build parent)
	{
		unitPriceProperty.bind(Bindings.when(unitPriceOverriddenProperty)
			.then(overriddenUnitPriceProperty).otherwise(parent.getUnitPrice()));
		
		unoverriddenTotalPriceBinding = Bindings.when(unitPriceOverriddenProperty)
			.then(overriddenUnitPriceProperty.multiply(((PositiveIntSpec) parent.getQuantity()).getIntProperty()))
			.otherwise(parent.getTotalPrice());
		
		totalPriceProperty.bind(
			Bindings.when(totalPriceOverriddenProperty)
			.then(overriddenTotalPriceProperty)
			.otherwise(unoverriddenTotalPriceBinding));
	}
	
	public void resetUnitPrice() { overriddenUnitPriceProperty.setValue(-Double.MAX_VALUE); }
	public void overrideUnitPrice(double price) { overriddenUnitPriceProperty.setValue(price); }
	
	public void resetTotalPrice() { overriddenTotalPriceProperty.setValue(-Double.MAX_VALUE); }
	public void overrideTotalPrice(double price) { overriddenTotalPriceProperty.setValue(price); }
}
