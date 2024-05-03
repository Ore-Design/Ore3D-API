package design.ore.Ore3DAPI.DataTypes.Protected;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import lombok.Getter;
import lombok.NonNull;

public class BuildPrice
{
	final ReadOnlyDoubleWrapper unitPriceProperty;
	public ReadOnlyDoubleProperty getUnitPrice() { return unitPriceProperty.getReadOnlyProperty(); }
	final ReadOnlyDoubleWrapper overriddenUnitPriceProperty;
	public ReadOnlyDoubleProperty getOverriddenUnitPrice() { return overriddenUnitPriceProperty.getReadOnlyProperty(); }
	@Getter final BooleanBinding unitPriceOverriddenProperty;

	final ReadOnlyDoubleWrapper totalPriceProperty;
	public ReadOnlyDoubleProperty getTotalPrice() { return totalPriceProperty.getReadOnlyProperty(); }
	final ReadOnlyDoubleWrapper overriddenTotalPriceProperty;
	public ReadOnlyDoubleProperty getOverriddenTotalPrice() { return overriddenTotalPriceProperty.getReadOnlyProperty(); }
	@Getter final BooleanBinding totalPriceOverriddenProperty;
	final ReadOnlyDoubleWrapper unoverriddenTotalPriceProperty = new ReadOnlyDoubleWrapper(-Double.MAX_VALUE);
	public ReadOnlyDoubleProperty getUnoverriddenTotalPriceProperty() { return unoverriddenTotalPriceProperty.getReadOnlyProperty(); };
	
	public BuildPrice(@NonNull Build parent)
	{
		unitPriceProperty = new ReadOnlyDoubleWrapper();
		overriddenUnitPriceProperty = new ReadOnlyDoubleWrapper(-Double.MAX_VALUE);
		
		totalPriceProperty = new ReadOnlyDoubleWrapper();
		overriddenTotalPriceProperty = new ReadOnlyDoubleWrapper(-Double.MAX_VALUE);
		
		unitPriceOverriddenProperty = overriddenUnitPriceProperty.greaterThan(-Double.MAX_VALUE);
		totalPriceOverriddenProperty = overriddenTotalPriceProperty.greaterThan(-Double.MAX_VALUE)
			.and(Bindings.createDoubleBinding(() -> new BigDecimal(unoverriddenTotalPriceProperty.get()).setScale(2, RoundingMode.HALF_UP)
			.doubleValue(), unoverriddenTotalPriceProperty).isEqualTo(overriddenTotalPriceProperty).not());
		
		rebindPricing(parent);
	}
	
	public void rebindPricing(Build parent)
	{
		unitPriceProperty.bind(Bindings.when(unitPriceOverriddenProperty)
			.then(overriddenUnitPriceProperty).otherwise(parent.getUnitPrice()));
		
		unoverriddenTotalPriceProperty.bind(Bindings.when(unitPriceOverriddenProperty)
			.then(overriddenUnitPriceProperty.multiply(parent.getQuantity()))
			.otherwise(parent.getTotalPrice()));
		
		totalPriceProperty.bind(
			Bindings.when(totalPriceOverriddenProperty)
			.then(overriddenTotalPriceProperty)
			.otherwise(unoverriddenTotalPriceProperty));
	}
	
	public void resetUnitPrice() { overriddenUnitPriceProperty.setValue(-Double.MAX_VALUE); }
	public void overrideUnitPrice(double price) { overriddenUnitPriceProperty.setValue(price); }
	
	public void resetTotalPrice() { overriddenTotalPriceProperty.setValue(-Double.MAX_VALUE); }
	public void overrideTotalPrice(double price) { overriddenTotalPriceProperty.setValue(price); }
}
