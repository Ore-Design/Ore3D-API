package design.ore.Ore3DAPI.DataTypes.Protected;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.Getter;
import lombok.NonNull;

public class BuildPrice
{
	@Getter final NumberBinding unitPrice;
	@Getter final NumberBinding unoverriddenTotalPriceProperty;
	
	protected final NumberBinding totalPrice;
	
	@Getter final BooleanBinding unitPriceOverriddenProperty;
	@Getter final BooleanBinding totalPriceOverriddenProperty;
	
	final ReadOnlyDoubleWrapper overriddenUnitPriceProperty;
	public ReadOnlyDoubleProperty getOverriddenUnitPrice() { return overriddenUnitPriceProperty.getReadOnlyProperty(); }
	final ReadOnlyDoubleWrapper overriddenTotalPriceProperty;
	public ReadOnlyDoubleProperty getOverriddenTotalPrice() { return overriddenTotalPriceProperty.getReadOnlyProperty(); }

	private final SimpleDoubleProperty parentQuantityProperty = new SimpleDoubleProperty(0);
	private final SimpleDoubleProperty parentUnitPriceProperty = new SimpleDoubleProperty(0);
	
	private final DoubleBinding roundedOverriddenUnitProperty;
	private final DoubleBinding roundedUnoverriddenUnitProperty;
	
	public BuildPrice(@NonNull Build parent)
	{
		overriddenUnitPriceProperty = new ReadOnlyDoubleWrapper(-Double.MAX_VALUE);
		unitPriceOverriddenProperty = overriddenUnitPriceProperty.greaterThan(-Double.MAX_VALUE);
		
		roundedOverriddenUnitProperty = Bindings.createDoubleBinding(() ->
		new BigDecimal(overriddenUnitPriceProperty.get()).setScale(2, RoundingMode.HALF_UP).doubleValue(), overriddenUnitPriceProperty);
		roundedUnoverriddenUnitProperty = Bindings.createDoubleBinding(() ->
		new BigDecimal(parentUnitPriceProperty.get()).setScale(2, RoundingMode.HALF_UP).doubleValue(), parentUnitPriceProperty);
		
		overriddenTotalPriceProperty = new ReadOnlyDoubleWrapper(-Double.MAX_VALUE);
		
		unoverriddenTotalPriceProperty = Bindings.when(unitPriceOverriddenProperty)
				.then(roundedOverriddenUnitProperty.multiply(parentQuantityProperty))
				.otherwise(roundedUnoverriddenUnitProperty.multiply(parentQuantityProperty));
		
		totalPriceOverriddenProperty = overriddenTotalPriceProperty.greaterThan(-Double.MAX_VALUE)
			.and(Bindings.createDoubleBinding(() -> new BigDecimal(unoverriddenTotalPriceProperty.getValue().doubleValue()).setScale(2, RoundingMode.HALF_UP)
			.doubleValue(), unoverriddenTotalPriceProperty).isEqualTo(overriddenTotalPriceProperty).not());
		
		unitPrice = Bindings.when(unitPriceOverriddenProperty)
			.then(roundedOverriddenUnitProperty).otherwise(roundedUnoverriddenUnitProperty);

		totalPrice = Bindings.when(totalPriceOverriddenProperty)
			.then(overriddenTotalPriceProperty)
			.otherwise(unoverriddenTotalPriceProperty);
		
		rebindPricing(parent);
	}
	
	public void rebindPricing(Build parent)
	{	
		parentUnitPriceProperty.bind(parent.getUnitPrice());
		parentQuantityProperty.bind(parent.getQuantity());
	}
	
	public void resetUnitPrice() { overriddenUnitPriceProperty.setValue(-Double.MAX_VALUE); }
	public void overrideUnitPrice(double price) { overriddenUnitPriceProperty.setValue(price); }
	
	public void resetTotalPrice() { overriddenTotalPriceProperty.setValue(-Double.MAX_VALUE); }
	public void overrideTotalPrice(double price) { overriddenTotalPriceProperty.setValue(price); }
}
