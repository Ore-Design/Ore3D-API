package design.ore.Ore3DAPI.DataTypes.Pricing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.DataTypes.Conflict;
import design.ore.Ore3DAPI.DataTypes.Interfaces.Conflictable;
import design.ore.Ore3DAPI.DataTypes.Interfaces.ValueStorageRecord;
import design.ore.Ore3DAPI.Jackson.ComponentSerialization;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonSerialize(using = ComponentSerialization.BOMs.Serializer.class)
@JsonDeserialize(using = ComponentSerialization.BOMs.Deserializer.class)
public class BOMEntry extends ValueStorageRecord implements Conflictable
{
	@Getter protected final String id;
	@Getter protected final String shortName;
	@Getter protected final String longName;
	@Getter protected final String unitOfMeasure;
	@Getter protected final double costPerQuantity;
	protected final ReadOnlyBooleanWrapper customEntry;
	public ReadOnlyBooleanProperty getCustomEntryProperty() { return customEntry.getReadOnlyProperty(); }
	
	@Getter protected final SimpleDoubleProperty unoverriddenQuantityProperty;
	@Getter protected final SimpleDoubleProperty overridenQuantityProperty;
	@Getter protected BooleanBinding quantityOverriddenProperty;

	@Getter protected final SimpleIntegerProperty unoverriddenMarginProperty;
	@Getter protected final SimpleIntegerProperty overridenMarginProperty;
	@Getter protected BooleanBinding marginOverriddenProperty;
	
	@Getter protected SimpleBooleanProperty ignoreParentQuantityProperty;

	protected final ReadOnlyDoubleWrapper quantityProperty;
	public ReadOnlyDoubleProperty getQuantityProperty() { return quantityProperty.getReadOnlyProperty(); }
	protected final ReadOnlyDoubleWrapper totalCostProperty;
	public ReadOnlyDoubleProperty getTotalCostProperty() { return totalCostProperty.getReadOnlyProperty(); }
	protected final ReadOnlyIntegerWrapper marginProperty;
	public ReadOnlyIntegerProperty getMarginProperty() { return marginProperty.getReadOnlyProperty(); }
	protected final DoubleBinding marginDenominatorProperty;
	@Getter protected final DoubleBinding totalPriceProperty;
	@Getter protected final DoubleBinding unitCostProperty;
	@Getter protected final DoubleBinding unitPriceProperty;
	
	@Getter protected ObservableList<Conflict> conflicts;
	
	public BOMEntry(String id, String shortName, String longName, String unitOfMeasure, double costPerQuantity, boolean customEntry, double quantity, int margin, boolean ignoreParentQuantity, ObservableNumberValue parentQuantity)
	{
		this.id = id;
		this.shortName = shortName;
		this.longName = longName;
		this.unitOfMeasure = unitOfMeasure;
		this.costPerQuantity = costPerQuantity;
		this.customEntry = new ReadOnlyBooleanWrapper(customEntry);
		this.conflicts = FXCollections.observableArrayList();
		
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

	@Override
	public void addConflict(Conflict conflict) { conflicts.add(conflict); }

	@Override
	public void clearConflicts() { conflicts.clear(); }
}
