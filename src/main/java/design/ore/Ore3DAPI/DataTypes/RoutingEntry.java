package design.ore.Ore3DAPI.DataTypes;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.DataTypes.Interfaces.Conflictable;
import design.ore.Ore3DAPI.Jackson.RoutingSerialization;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
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
@JsonSerialize(using = RoutingSerialization.Serializer.class)
@JsonDeserialize(using = RoutingSerialization.Deserializer.class)
public class RoutingEntry implements Conflictable
{
	protected final Logger log;
	
	@Getter protected final String id;
	@Getter protected final String name;
	@Getter protected final double costPerQuantity;
	
	@Getter protected final SimpleDoubleProperty unoverriddenQuantityProperty;
	@Getter protected final SimpleDoubleProperty overridenQuantityProperty;
	@Getter protected BooleanBinding quantityOverriddenProperty;

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
	
	public RoutingEntry(Logger log, String id, String name, double costPerQuantity, double quantity, int margin, ObservableNumberValue parentQuantity)
	{
		this.id = id;
		this.name = name;
		this.costPerQuantity = costPerQuantity;
		this.conflicts = FXCollections.observableArrayList();
		this.log = log;
		
		this.unoverriddenQuantityProperty = new SimpleDoubleProperty(quantity);
		this.overridenQuantityProperty = new SimpleDoubleProperty(-1.0);
		this.quantityOverriddenProperty = overridenQuantityProperty.greaterThanOrEqualTo(0.0).and(overridenQuantityProperty.isEqualTo(unoverriddenQuantityProperty).not());
		
		quantityProperty = new ReadOnlyDoubleWrapper();

		if(quantityOverriddenProperty.get()) this.quantityProperty.bind(overridenQuantityProperty);
		else this.quantityProperty.bind(unoverriddenQuantityProperty);
		
		quantityOverriddenProperty.addListener((observable, oldValue, newValue) ->
		{
			if(log != null) log.debug("QuantityOverriden property has been changed! Updating bindings...");
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
	
	public RoutingEntry(Logger log, String id, String name, double costPerQuantity, int margin, ObservableNumberValue parentQuantity)
	{
		this(log, id, name, costPerQuantity, 0.0, margin, parentQuantity);
	}
	
	public RoutingEntry(String id, String name, double qty, double overriddenQty, int margin)
	{
		this(null, id, "", 0, qty, margin, new SimpleDoubleProperty(0));
		this.overridenQuantityProperty.set(overriddenQty);
	}

	public RoutingEntry duplicate(double newQuantity, ObservableNumberValue parentQuantity)
	{ return new RoutingEntry(log, id, name, costPerQuantity, newQuantity, marginProperty.get(), parentQuantity); }
	
	public RoutingEntry duplicate(ObservableNumberValue parentQuantity)
	{ return new RoutingEntry(log, id, name, costPerQuantity, unoverriddenQuantityProperty.get(), marginProperty.get(), parentQuantity); }

	@Override
	public void addConflict(Conflict conflict) { conflicts.add(conflict); }

	@Override
	public void clearConflicts() { conflicts.clear(); }
}
