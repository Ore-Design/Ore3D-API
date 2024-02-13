package design.ore.Ore3DAPI.DataTypes.Pricing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.Util.Mapper;
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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonSerialize(using = ComponentSerialization.Routings.Serializer.class)
@JsonDeserialize(using = ComponentSerialization.Routings.Deserializer.class)
public class RoutingEntry extends ValueStorageRecord implements Conflictable
{
	@Getter protected final String id;
	@Getter protected final String name;
	@Getter protected double costPerQuantity;
	
	@Getter protected final SimpleDoubleProperty unoverriddenQuantityProperty;
	@Getter protected final SimpleDoubleProperty overridenQuantityProperty;
	@Getter protected BooleanBinding quantityOverriddenProperty;
	
	protected final ReadOnlyBooleanWrapper customEntry;
	public ReadOnlyBooleanProperty getCustomEntryProperty() { return customEntry.getReadOnlyProperty(); }

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
	
	public RoutingEntry(String id, String name, double costPerQuantity, double quantity, int margin, ObservableNumberValue parentQuantity, boolean customEntry)
	{
		this.id = id;
		this.name = name;
		this.costPerQuantity = costPerQuantity;
		this.conflicts = FXCollections.observableArrayList();
		this.customEntry = new ReadOnlyBooleanWrapper(customEntry);
		
		this.unoverriddenQuantityProperty = new SimpleDoubleProperty(quantity);
		this.overridenQuantityProperty = new SimpleDoubleProperty(-1.0);
		this.quantityOverriddenProperty = overridenQuantityProperty.greaterThanOrEqualTo(0.0).and(this.customEntry.not()).and(overridenQuantityProperty.isEqualTo(unoverriddenQuantityProperty).not());
//		this.quantityOverriddenProperty = overridenQuantityProperty.greaterThanOrEqualTo(0.0).and(overridenQuantityProperty.isEqualTo(unoverriddenQuantityProperty).not());
		
		quantityProperty = new ReadOnlyDoubleWrapper();

		quantityProperty.bind(Bindings.when(quantityOverriddenProperty).then(overridenQuantityProperty).otherwise(unoverriddenQuantityProperty));
//		if(quantityOverriddenProperty.get()) this.quantityProperty.bind(overridenQuantityProperty);
//		else this.quantityProperty.bind(unoverriddenQuantityProperty);
		
		quantityOverriddenProperty.addListener((observable, oldValue, newValue) ->
		{
			if(newValue) this.quantityProperty.bind(overridenQuantityProperty);
			else this.quantityProperty.bind(unoverriddenQuantityProperty);
		});
		
		this.unitCostProperty = quantityProperty.multiply(costPerQuantity);
		
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
			if(newCostPerQuantity != null) newEntry.costPerQuantity = newCostPerQuantity;
			if(newQuantity != null) newEntry.unoverriddenQuantityProperty.setValue(newQuantity);
			if(parentQuantity != null) newEntry.totalCostProperty.bind(newEntry.unitCostProperty.multiply(parentQuantity));
			if(overriddenQuantity != null) newEntry.overridenQuantityProperty.set(overriddenQuantity);;
			return newEntry;
		}
		catch (Exception e) { Log.getLogger().error("Error duplicationg routing entry:\n" + Util.stackTraceArrayToString(e)); }
		return null;
	}

	@Override
	public void addConflict(Conflict conflict) { conflicts.add(conflict); }

	@Override
	public void clearConflicts() { conflicts.clear(); }
}
