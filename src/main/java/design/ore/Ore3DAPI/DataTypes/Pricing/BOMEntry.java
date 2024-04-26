package design.ore.Ore3DAPI.DataTypes.Pricing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import design.ore.Ore3DAPI.Util;
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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableNumberValue;
import lombok.Getter;
import lombok.Setter;

@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class BOMEntry extends ValueStorageRecord
{
	public BOMEntry() { this("", "", "", "", 0, true, 0, 0, false, Util.zeroDoubleBinding()); }
	
	@Getter @Setter protected String id;
	@JsonProperty("sn") @Getter @Setter protected String shortName;
	@JsonProperty("ln") @Getter @Setter protected String longName;
	@JsonProperty("uom") @Getter @Setter protected String unitOfMeasure;
	@JsonProperty("cpq") @Getter @Setter protected double costPerQuantity;
	
	protected final ReadOnlyBooleanWrapper customEntry;
	@JsonIgnore public ReadOnlyBooleanProperty getCustomEntryProperty() { return customEntry.getReadOnlyProperty(); }
	@JsonProperty("cust") public boolean isCustomEntry() { return customEntry.get(); }
	@JsonProperty("cust") public void setCustomEntry(boolean val) { customEntry.set(val); }
	
	@JsonIgnore @Getter protected final SimpleDoubleProperty unoverriddenQuantityProperty;
	@JsonProperty("q") public double getQuantity() { return unoverriddenQuantityProperty.get(); }
	@JsonProperty("q") public void setQuantity(int val) { unoverriddenQuantityProperty.set(val); }
	@JsonIgnore @Getter protected final SimpleDoubleProperty overridenQuantityProperty;
	@JsonProperty("ovrq") public double getOverriddenQuantity() { return overridenQuantityProperty.get(); }
	@JsonProperty("ovrq") public void setOverriddenQuantity(int val) { overridenQuantityProperty.set(val); }
	@JsonIgnore @Getter protected BooleanBinding quantityOverriddenProperty;

	@JsonIgnore @Getter protected final SimpleIntegerProperty unoverriddenMarginProperty;
	@JsonProperty("m") public int getMargin() { return unoverriddenMarginProperty.get(); }
	@JsonProperty("m") public void setMargin(int val) { unoverriddenMarginProperty.set(val); }
	@JsonIgnore @Getter protected final SimpleIntegerProperty overridenMarginProperty;
	@JsonProperty("ovrm") public int getOverriddenMargin() { return overridenMarginProperty.get(); }
	@JsonProperty("ovrm") public void setOverriddenMargin(int val) { overridenMarginProperty.set(val); }
	@JsonIgnore @Getter protected BooleanBinding marginOverriddenProperty;
	
	@JsonIgnore @Getter protected SimpleBooleanProperty ignoreParentQuantityProperty;
	@JsonProperty("ipq") public boolean getIgnoreParentQuantity() { return ignoreParentQuantityProperty.get(); }
	@JsonProperty("ipq") public void setIgnoreParentQuantity(boolean val) { ignoreParentQuantityProperty.set(val); }

	protected final ReadOnlyDoubleWrapper quantityProperty;
	@JsonIgnore public ReadOnlyDoubleProperty getQuantityProperty() { return quantityProperty.getReadOnlyProperty(); }
	protected final ReadOnlyDoubleWrapper totalCostProperty;
	@JsonIgnore public ReadOnlyDoubleProperty getTotalCostProperty() { return totalCostProperty.getReadOnlyProperty(); }
	protected final ReadOnlyIntegerWrapper marginProperty;
	@JsonIgnore public ReadOnlyIntegerProperty getMarginProperty() { return marginProperty.getReadOnlyProperty(); }
	
	protected final DoubleBinding marginDenominatorProperty;
	@JsonIgnore @Getter protected final DoubleBinding totalPriceProperty;
	@JsonIgnore @Getter protected final DoubleBinding unitCostProperty;
	@JsonIgnore @Getter protected final DoubleBinding unitPriceProperty;
	
	public BOMEntry(String id, String shortName, String longName, String unitOfMeasure, double costPerQuantity, boolean customEntry, double quantity, int margin, boolean ignoreParentQuantity, ObservableNumberValue parentQuantity)
	{
		this.id = id;
		this.shortName = shortName;
		this.longName = longName;
		this.unitOfMeasure = unitOfMeasure;
		this.costPerQuantity = costPerQuantity;
		this.customEntry = new ReadOnlyBooleanWrapper(customEntry);
		
		this.unoverriddenQuantityProperty = new SimpleDoubleProperty(quantity);
		this.overridenQuantityProperty = new SimpleDoubleProperty(-1.0);
		this.quantityOverriddenProperty = overridenQuantityProperty.greaterThanOrEqualTo(0.0).and(this.customEntry.not()).and(overridenQuantityProperty.isEqualTo(unoverriddenQuantityProperty).not());

		this.unoverriddenMarginProperty = new SimpleIntegerProperty(margin);
		this.overridenMarginProperty = new SimpleIntegerProperty(-1);
		this.marginOverriddenProperty = overridenMarginProperty.greaterThanOrEqualTo(0.0);
		
		this.ignoreParentQuantityProperty = new SimpleBooleanProperty(ignoreParentQuantity);
		
		quantityProperty = new ReadOnlyDoubleWrapper();
		quantityProperty.bind(Bindings.when(quantityOverriddenProperty).then(overridenQuantityProperty).otherwise(unoverriddenQuantityProperty));
		
		this.unitCostProperty = quantityProperty.multiply(costPerQuantity);
		
		this.totalCostProperty = new ReadOnlyDoubleWrapper();
		totalCostProperty.bind(Bindings.when(ignoreParentQuantityProperty).then(unitCostProperty).otherwise(unitCostProperty.multiply(parentQuantity)));
		
		this.marginProperty = new ReadOnlyIntegerWrapper();
		marginProperty.bind(Bindings.when(marginOverriddenProperty).then(overridenMarginProperty).otherwise(unoverriddenMarginProperty));
		
		this.marginDenominatorProperty = new ReadOnlyDoubleWrapper(1.0).subtract(marginProperty.getReadOnlyProperty().divide(100.0));
		this.totalPriceProperty = totalCostProperty.getReadOnlyProperty().divide(marginDenominatorProperty);
		this.unitPriceProperty = unitCostProperty.divide(marginDenominatorProperty);
	}
	
	public BOMEntry(String id, boolean custom, boolean ignoreParentQuantity, double qty, double overriddenQty, int margin, int overriddenMargin)
	{
		this(id, "", "", "", 0, custom, qty, margin, ignoreParentQuantity, new SimpleDoubleProperty(0));
		this.overridenQuantityProperty.set(overriddenQty);
		this.overridenMarginProperty.set(overriddenMargin);
	}
	
	public BOMEntry(String id, String shortName, String longName, String unitOfMeasure, double costPerQuantity, boolean custom, int margin, boolean ignoreParentQuantity, ObservableNumberValue parentQuantity)
	{
		this(id, shortName, longName, unitOfMeasure, costPerQuantity, custom, 0.0, margin, ignoreParentQuantity, parentQuantity);
	}

	public BOMEntry duplicate(double newCostPerQuantity, double newQuantity, ObservableNumberValue parentQuantity, boolean isCustom, boolean ignoreParentQuantity)
	{
		BOMEntry newEntry = new BOMEntry(id, shortName, longName, unitOfMeasure, newCostPerQuantity, isCustom, newQuantity, unoverriddenMarginProperty.get(), ignoreParentQuantity, parentQuantity);
		newEntry.putStoredValues(getStoredValues());
		return newEntry;
	}

	public BOMEntry duplicate(double newCostPerQuantity, double newQuantity, ObservableNumberValue parentQuantity, boolean isCustom)
	{
		BOMEntry newEntry = new BOMEntry(id, shortName, longName, unitOfMeasure, newCostPerQuantity, isCustom, newQuantity, unoverriddenMarginProperty.get(), ignoreParentQuantityProperty.get(), parentQuantity);
		newEntry.putStoredValues(getStoredValues());
		return newEntry;
	}

	public BOMEntry duplicate(double newQuantity, ObservableNumberValue parentQuantity, boolean isCustom, boolean ignoreParentQuantity)
	{
		BOMEntry newEntry = new BOMEntry(id, shortName, longName, unitOfMeasure, costPerQuantity, isCustom, newQuantity, unoverriddenMarginProperty.get(), ignoreParentQuantity, parentQuantity);
		newEntry.putStoredValues(getStoredValues());
		return newEntry;
	}

	public BOMEntry duplicate(double newQuantity, ObservableNumberValue parentQuantity, boolean isCustom)
	{
		BOMEntry newEntry = new BOMEntry(id, shortName, longName, unitOfMeasure, costPerQuantity, isCustom, newQuantity, unoverriddenMarginProperty.get(), ignoreParentQuantityProperty.get(), parentQuantity);
		newEntry.putStoredValues(getStoredValues());
		return newEntry;
	}

	public BOMEntry duplicate(double newQuantity, ObservableNumberValue parentQuantity)
	{
		BOMEntry newEntry = new BOMEntry(id, shortName, longName, unitOfMeasure, costPerQuantity, customEntry.get(), newQuantity, unoverriddenMarginProperty.get(), ignoreParentQuantityProperty.get(), parentQuantity);
		newEntry.putStoredValues(getStoredValues());
		return newEntry;
	}
	
	public BOMEntry duplicate(ObservableNumberValue parentQuantity)
	{
		BOMEntry newEntry = new BOMEntry(id, shortName, longName, unitOfMeasure, costPerQuantity, customEntry.get(), unoverriddenQuantityProperty.get(),
			unoverriddenMarginProperty.get(), ignoreParentQuantityProperty.get(), parentQuantity);
		newEntry.putStoredValues(getStoredValues());
		return newEntry;
	}
}
