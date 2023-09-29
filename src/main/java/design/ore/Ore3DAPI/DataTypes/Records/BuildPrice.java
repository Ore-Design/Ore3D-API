package design.ore.Ore3DAPI.DataTypes.Records;

import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
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
		
		rebindTotalPrice(parent.getTotalPrice(), ((PositiveIntSpec) parent.getQuantity()).getIntProperty());

		parent.getBom().addListener((ListChangeListener.Change<?> c) ->
		{
			rebindTotalPrice(parent.getTotalPrice(), ((PositiveIntSpec) parent.getQuantity()).getIntProperty());
			rebindUnitPrice(parent.getUnitPrice());
		});
		parent.getRoutings().addListener((ListChangeListener.Change<?> c) ->
		{
			rebindTotalPrice(parent.getTotalPrice(), ((PositiveIntSpec) parent.getQuantity()).getIntProperty());
			rebindUnitPrice(parent.getUnitPrice());
		});
		unitPriceOverriddenProperty.addListener((observable, oldVal, newVal) ->
		{
			rebindTotalPrice(parent.getTotalPrice(), ((PositiveIntSpec) parent.getQuantity()).getIntProperty());
			rebindUnitPrice(parent.getUnitPrice());
		});
	}
	
	private void rebindUnitPrice(DoubleBinding newBind)
	{
		if(unitPriceOverriddenProperty.get()) unitPriceProperty.bind(overriddenUnitPriceProperty);
		else unitPriceProperty.bind(newBind);
	}
	
	private void rebindTotalPrice(DoubleBinding newBind, IntegerProperty quantityBinding)
	{
		if(unitPriceOverriddenProperty.get()) totalPrice = overriddenUnitPriceProperty.multiply(quantityBinding);
		else totalPrice = newBind;
	}
	
	public void reset() { overriddenUnitPriceProperty.setValue(-1.0); }
	public void override(double price)
	{
		unitPriceProperty.setValue(price);
	}
}
