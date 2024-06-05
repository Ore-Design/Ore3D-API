package design.ore.Ore3DAPI.DataTypes.Pricing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import design.ore.Ore3DAPI.Registry;
import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.Util.Mapper;
import design.ore.Ore3DAPI.DataTypes.Interfaces.ValueStorageRecord;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableNumberValue;
import lombok.Getter;
import lombok.Setter;

@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class RoutingEntry extends ValueStorageRecord
{
	public RoutingEntry() { this("", "", 0, 0, 0, Util.zeroDoubleBinding(), false); }
	
	@Getter @Setter protected String id;
	@JsonProperty("n") @Getter @Setter protected String name;
	@JsonIgnore @Getter protected SimpleDoubleProperty costPerQuantityProperty;
	@JsonProperty("cpq") public double getCostPerQuantity() { return costPerQuantityProperty.get(); }
	@JsonProperty("cpq") public void setCostPerQuantity(double cost) { costPerQuantityProperty.set(cost); }
	
	protected final ReadOnlyBooleanWrapper customEntry;
	@JsonIgnore public ReadOnlyBooleanProperty getCustomEntryProperty() { return customEntry.getReadOnlyProperty(); }
	@JsonProperty("cust") public boolean isCustomEntry() { return customEntry.get(); }
	@JsonProperty("cust") public void setCustomEntry(boolean val) { customEntry.set(val); }
	
	@JsonIgnore @Getter protected final SimpleDoubleProperty unoverriddenQuantityProperty;
	@JsonProperty("q") public double getQuantity() { return unoverriddenQuantityProperty.get(); }
	@JsonProperty("q") public void setQuantity(double val) { unoverriddenQuantityProperty.set(val); }
	@JsonIgnore @Getter protected final SimpleDoubleProperty overridenQuantityProperty;
	@JsonProperty("ovrq") public double getOverriddenQuantity() { return overridenQuantityProperty.get(); }
	@JsonProperty("ovrq") public void setOverriddenQuantity(double val) { overridenQuantityProperty.set(val); }
	@JsonIgnore @Getter protected BooleanBinding quantityOverriddenProperty;

	protected final ReadOnlyDoubleWrapper quantityProperty;
	@JsonIgnore public ReadOnlyDoubleProperty getQuantityProperty() { return quantityProperty.getReadOnlyProperty(); }
	protected final ReadOnlyDoubleWrapper totalCostProperty;
	@JsonIgnore public ReadOnlyDoubleProperty getTotalCostProperty() { return totalCostProperty.getReadOnlyProperty(); }
	protected final ReadOnlyIntegerWrapper marginProperty;
	@JsonProperty("m") public int getMargin() { return marginProperty.get(); }
	@JsonProperty("m") public void setMargin(int val) { marginProperty.set(val); }
	@JsonIgnore public ReadOnlyIntegerProperty getMarginProperty() { return marginProperty.getReadOnlyProperty(); }

	protected final DoubleBinding marginDenominatorProperty;
	@JsonIgnore @Getter protected final DoubleBinding totalPriceProperty;
	@JsonIgnore @Getter protected final DoubleBinding unitCostProperty;
	@JsonIgnore @Getter protected final DoubleBinding unitPriceProperty;
	
	public RoutingEntry(String id, String name, double costPerQuantity, double quantity, int margin, ObservableNumberValue parentQuantity, boolean customEntry)
	{
		this.id = id;
		this.name = name;
		this.costPerQuantityProperty = new SimpleDoubleProperty();
		this.customEntry = new ReadOnlyBooleanWrapper(customEntry);
		
		this.unoverriddenQuantityProperty = new SimpleDoubleProperty(quantity);
		this.overridenQuantityProperty = new SimpleDoubleProperty(-1.0);
		this.quantityOverriddenProperty = overridenQuantityProperty.greaterThanOrEqualTo(0.0).and(this.customEntry.not()).and(overridenQuantityProperty.isEqualTo(unoverriddenQuantityProperty).not());
		
		quantityProperty = new ReadOnlyDoubleWrapper();
		quantityProperty.bind(Bindings.when(quantityOverriddenProperty).then(overridenQuantityProperty).otherwise(unoverriddenQuantityProperty));
		
		this.costPerQuantityProperty.setValue(costPerQuantity);
		this.unitCostProperty = quantityProperty.multiply(costPerQuantityProperty);
		
		this.totalCostProperty = new ReadOnlyDoubleWrapper();
		
		totalCostProperty.bind(unitCostProperty.multiply(parentQuantity));
		
		this.marginProperty = new ReadOnlyIntegerWrapper(margin);
		
		this.marginDenominatorProperty = new ReadOnlyDoubleWrapper(1.0).subtract(marginProperty.getReadOnlyProperty().divide(100.0));
		this.totalPriceProperty = totalCostProperty.getReadOnlyProperty().divide(marginDenominatorProperty);
		this.unitPriceProperty = unitCostProperty.divide(marginDenominatorProperty);
	}
	
	public RoutingEntry(String id, String name, double costPerQuantity, int margin, ObservableNumberValue parentQuantity, boolean customEntry)
	{
		this(id, name, costPerQuantity, 0.0, margin, parentQuantity, customEntry);
	}
	
	public RoutingEntry(String id, String name, double qty, double overriddenQty, int margin, boolean customEntry)
	{
		this(id, "", 0, qty, margin, new SimpleDoubleProperty(0), customEntry);
		this.overridenQuantityProperty.set(overriddenQty);
	}

	public RoutingEntry duplicate(Double newCostPerQuantity, Double newQuantity, ObservableNumberValue parentQuantity, Boolean isCustom, Double overriddenQuantity)
	{
		try
		{
			String json = Mapper.getMapper().writeValueAsString(this);
			RoutingEntry newEntry = Mapper.getMapper().readValue(json, RoutingEntry.class);
			if(isCustom != null) newEntry.customEntry.set(isCustom);
			if(newCostPerQuantity != null) newEntry.setCostPerQuantity(newCostPerQuantity);
			if(newQuantity != null) newEntry.unoverriddenQuantityProperty.setValue(newQuantity);
			if(parentQuantity != null) newEntry.totalCostProperty.bind(newEntry.unitCostProperty.multiply(parentQuantity));
			if(overriddenQuantity != null) newEntry.overridenQuantityProperty.set(overriddenQuantity);
			Registry.handleRoutingDuplicate(newEntry);
			return newEntry;
		}
		catch (Exception e) { Log.getLogger().error("Error duplicating routing entry:\n" + Util.stackTraceArrayToString(e)); }
		return null;
	}
}
