package design.ore.Ore3DAPI.DataTypes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import design.ore.Ore3DAPI.DataTypes.Interfaces.Conflictable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class BOMEntry implements Conflictable
{
	@Getter protected final String id;
	@Getter protected final String displayName;
	@Getter protected final double costPerQuantity;
	@Getter protected final ReadOnlyBooleanWrapper customEntry;
	
	@Getter protected final SimpleDoubleProperty unoverriddenQuantityProperty;
	@Getter protected final SimpleDoubleProperty overridenQuantityProperty;
	@Getter protected BooleanBinding quantityOverriddenProperty;

	@Getter protected final SimpleIntegerProperty unoverriddenMarginProperty;
	@Getter protected final SimpleIntegerProperty overridenMarginProperty;
	@Getter protected BooleanBinding marginOverriddenProperty;
	@Getter protected final DoubleBinding marginDenominatorProperty;
	
	@Getter protected SimpleBooleanProperty ignoreParentQuantityProperty;

	@Getter protected final DoubleBinding quantityProperty;
	@Getter protected final DoubleBinding totalPriceProperty;
	protected final DoubleBinding totalCostProperty;
	
	@Getter protected List<Conflict> conflicts;
	
	public BOMEntry(String id, String displayName, double costPerQuantity, boolean customEntry, double quantity, int margin, boolean ignoreParentQuantity, ObservableIntegerValue parentQuantity)
	{
		this.id = id;
		this.displayName = displayName;
		this.costPerQuantity = costPerQuantity;
		this.customEntry = new ReadOnlyBooleanWrapper(customEntry);
		this.conflicts = new ArrayList<Conflict>();
		
		this.unoverriddenQuantityProperty = new SimpleDoubleProperty(quantity);
		this.overridenQuantityProperty = new SimpleDoubleProperty(-1.0);
		this.quantityOverriddenProperty = overridenQuantityProperty.greaterThanOrEqualTo(0.0).and(this.customEntry);

		this.unoverriddenMarginProperty = new SimpleIntegerProperty(margin);
		this.overridenMarginProperty = new SimpleIntegerProperty(-1);
		this.marginOverriddenProperty = overridenMarginProperty.greaterThanOrEqualTo(0.0);
		
		this.ignoreParentQuantityProperty = new SimpleBooleanProperty(ignoreParentQuantity);
		
		this.quantityProperty = Bindings.when(quantityOverriddenProperty).then(overridenQuantityProperty).otherwise(costPerQuantity);
		
		this.totalCostProperty = (DoubleBinding) Bindings.when(ignoreParentQuantityProperty).then(quantityProperty.multiply(costPerQuantity))
				.otherwise(unoverriddenQuantityProperty.multiply(costPerQuantity).multiply(parentQuantity));
		
		this.marginDenominatorProperty = (DoubleBinding) Bindings.when(marginOverriddenProperty).then().otherwise();
		
		this.totalPriceProperty = totalCostProperty.divide(marginDenominatorProperty);
	}
	
	public BOMEntry(String id, String displayName, double costPerQuantity, boolean custom, int margin, boolean ignoreParentQuantity, ObservableIntegerValue parentQuantity)
	{
		this(id, displayName, costPerQuantity, custom, 0.0, margin, ignoreParentQuantity, parentQuantity);
	}

	public BOMEntry duplicate(double newQuantity, ObservableIntegerValue parentQuantity)
	{ return new BOMEntry(id, displayName, costPerQuantity, customEntry, newQuantity, ignoreParentQuantityProperty.get(), parentQuantity); }
	
	public BOMEntry duplicate(ObservableIntegerValue parentQuantity)
	{ return new BOMEntry(id, displayName, costPerQuantity, customEntry, unoverriddenQuantityProperty.get(), ignoreParentQuantityProperty.get(), parentQuantity); }
	
	public void overrideQuantity(double quantity) { overridenQuantityProperty.setValue(quantity); }
	public void revertQuantityOverride() { overridenQuantityProperty.set(-1.0); }

	@Override
	public void addConflict(Conflict conflict) { conflicts.add(conflict); }

	@Override
	public void clearConflicts() { conflicts.clear(); }
}
