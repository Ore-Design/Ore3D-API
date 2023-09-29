package design.ore.Ore3DAPI.DataTypes;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import design.ore.Ore3DAPI.DataTypes.Interfaces.Conflictable;
import design.ore.Ore3DAPI.DataTypes.Records.ValueStorageRecord;
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
public class BOMEntry extends ValueStorageRecord implements Conflictable
{
	protected final Logger log;
	
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
	
	public BOMEntry(Logger log, String id, String shortName, String longName, String unitOfMeasure, double costPerQuantity, boolean customEntry, double quantity, int margin, boolean ignoreParentQuantity, ObservableNumberValue parentQuantity)
	{
		this.id = id;
		this.shortName = shortName;
		this.longName = longName;
		this.unitOfMeasure = unitOfMeasure;
		this.costPerQuantity = costPerQuantity;
		this.customEntry = new ReadOnlyBooleanWrapper(customEntry);
		this.conflicts = FXCollections.observableArrayList();
		this.log = log;
		
		this.unoverriddenQuantityProperty = new SimpleDoubleProperty(quantity);
		this.overridenQuantityProperty = new SimpleDoubleProperty(-1.0);
		this.quantityOverriddenProperty = overridenQuantityProperty.greaterThanOrEqualTo(0.0).and(this.customEntry.not()).and(overridenQuantityProperty.isEqualTo(unoverriddenQuantityProperty).not());

		this.unoverriddenMarginProperty = new SimpleIntegerProperty(margin);
		this.overridenMarginProperty = new SimpleIntegerProperty(-1);
		this.marginOverriddenProperty = overridenMarginProperty.greaterThanOrEqualTo(0.0);
		
		this.ignoreParentQuantityProperty = new SimpleBooleanProperty(ignoreParentQuantity);
		
		quantityProperty = new ReadOnlyDoubleWrapper();

		if(quantityOverriddenProperty.get()) this.quantityProperty.bind(overridenQuantityProperty);
		else this.quantityProperty.bind(unoverriddenQuantityProperty);
		
		quantityOverriddenProperty.addListener((observable, oldValue, newValue) ->
		{
			log.debug("QuantityOverriden property has been changed! Updating bindings...");
			if(newValue) this.quantityProperty.bind(overridenQuantityProperty);
			else this.quantityProperty.bind(unoverriddenQuantityProperty);
		});
		
		this.unitCostProperty = quantityProperty.multiply(costPerQuantity);
		
		this.totalCostProperty = new ReadOnlyDoubleWrapper();
		
		if(ignoreParentQuantityProperty.get()) this.totalCostProperty.bind(unitCostProperty);
		else this.totalCostProperty.bind(unitCostProperty.multiply(parentQuantity));
		
		ignoreParentQuantityProperty.addListener((observable, oldValue, newValue) ->
		{
			log.debug("IgnoreParentQuantity property has been changed! Updating bindings...");
			if(newValue) this.totalCostProperty.bind(unitCostProperty);
			else this.totalCostProperty.bind(unitCostProperty.multiply(parentQuantity));
		});
		
		
		this.marginProperty = new ReadOnlyIntegerWrapper();
		
		if(marginOverriddenProperty.get()) this.marginProperty.bind(overridenMarginProperty.add(0));
		else this.marginProperty.bind(unoverriddenMarginProperty.add(0));
		
		marginOverriddenProperty.addListener((observable, oldValue, newValue) ->
		{
			log.debug("MarginOverridden property has been changed! Updating bindings...");
			if(newValue) this.marginProperty.bind(overridenMarginProperty.add(0));
			else this.marginProperty.bind(unoverriddenMarginProperty.add(0));
		});
		
		this.marginDenominatorProperty = new ReadOnlyDoubleWrapper(1.0).subtract(marginProperty.getReadOnlyProperty().divide(100.0));
		this.totalPriceProperty = totalCostProperty.getReadOnlyProperty().divide(marginDenominatorProperty);
		this.unitPriceProperty = unitCostProperty.divide(marginDenominatorProperty);
	}
	
	public BOMEntry(Logger log, String id, String shortName, String longName, String unitOfMeasure, double costPerQuantity, boolean custom, int margin, boolean ignoreParentQuantity, ObservableNumberValue parentQuantity)
	{
		this(log, id, shortName, longName, unitOfMeasure, costPerQuantity, custom, 0.0, margin, ignoreParentQuantity, parentQuantity);
	}

	public BOMEntry duplicate(double newQuantity, ObservableNumberValue parentQuantity, boolean isCustom, boolean ignoreParentQuantity)
	{ return new BOMEntry(log, id, shortName, longName, unitOfMeasure, costPerQuantity, isCustom, newQuantity, unoverriddenMarginProperty.get(), ignoreParentQuantity, parentQuantity); }

	public BOMEntry duplicate(double newQuantity, ObservableNumberValue parentQuantity)
	{ return new BOMEntry(log, id, shortName, longName, unitOfMeasure, costPerQuantity, customEntry.get(), newQuantity, unoverriddenMarginProperty.get(), ignoreParentQuantityProperty.get(), parentQuantity); }
	
	public BOMEntry duplicate(ObservableNumberValue parentQuantity)
	{ return new BOMEntry(log, id, shortName, longName, unitOfMeasure, costPerQuantity, customEntry.get(), unoverriddenQuantityProperty.get(), unoverriddenMarginProperty.get(), ignoreParentQuantityProperty.get(), parentQuantity); }

	@Override
	public void addConflict(Conflict conflict) { conflicts.add(conflict); }

	@Override
	public void clearConflicts() { conflicts.clear(); }
}
