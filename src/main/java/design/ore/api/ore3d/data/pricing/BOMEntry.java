package design.ore.api.ore3d.data.pricing;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import design.ore.api.ore3d.Registry;
import design.ore.api.ore3d.data.core.Build;
import design.ore.api.ore3d.data.interfaces.ISummaryOption;
import design.ore.api.ore3d.data.interfaces.ValueStorageRecord;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;

@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class BOMEntry extends ValueStorageRecord implements ISummaryOption
{
	public BOMEntry() { this("", "", "", "", 0, true, 0, 0, false, null); }
	
	@Getter @Setter protected String id;
	@JsonProperty("sn") @Getter @Setter protected String shortName;
	@JsonProperty("ln") @Getter @Setter protected String longName;
	@JsonProperty("uom") @Getter @Setter protected String unitOfMeasure;
	
	@JsonIgnore @Getter protected SimpleDoubleProperty costPerQuantityProperty;
	@JsonProperty("cpq") public double getCostPerQuantity() { return costPerQuantityProperty.get(); }
	@JsonProperty("cpq") public void setCostPerQuantity(double val) { costPerQuantityProperty.set(val); }
	
	protected ReadOnlyBooleanWrapper customEntry;
	@JsonIgnore public ReadOnlyBooleanProperty getCustomEntryProperty() { return customEntry.getReadOnlyProperty(); }
	@JsonProperty("cust") public boolean isCustomEntry() { return customEntry.get(); }
	@JsonProperty("cust") public void setCustomEntry(boolean val) { customEntry.set(val); }
	
	@JsonIgnore @Getter protected SimpleDoubleProperty unoverriddenQuantityProperty;
	@JsonProperty("q") public double getQuantity() { return unoverriddenQuantityProperty.get(); }
	@JsonProperty("q") public void setQuantity(double val) { unoverriddenQuantityProperty.set(val); }
	@JsonIgnore @Getter protected SimpleDoubleProperty overridenQuantityProperty;
	@JsonProperty("ovrq") public double getOverriddenQuantity() { return overridenQuantityProperty.get(); }
	@JsonProperty("ovrq") public void setOverriddenQuantity(double val) { overridenQuantityProperty.set(val); }
	@JsonIgnore @Getter protected BooleanBinding quantityOverriddenProperty;

	@JsonIgnore @Getter protected SimpleIntegerProperty unoverriddenMarginProperty;
	@JsonProperty("m") public int getUnoverriddenMargin() { return unoverriddenMarginProperty.get(); }
	@JsonProperty("m") public void setUnoverriddenMargin(int val) { unoverriddenMarginProperty.set(val); }
	@JsonIgnore @Getter protected SimpleIntegerProperty overridenMarginProperty;
	@JsonProperty("ovrm") public int getOverriddenMargin() { return overridenMarginProperty.get(); }
	@JsonProperty("ovrm") public void setOverriddenMargin(int val) { overridenMarginProperty.set(val); }
	@JsonIgnore @Getter protected BooleanBinding marginOverriddenProperty;
	
	@JsonIgnore @Getter protected SimpleBooleanProperty ignoreParentQuantityProperty;
	@JsonProperty("ipq") public boolean getIgnoreParentQuantity() { return ignoreParentQuantityProperty.get(); }
	@JsonProperty("ipq") public void setIgnoreParentQuantity(boolean val) { ignoreParentQuantityProperty.set(val); }
	
	@Getter protected final List<String> searchableTags = new ArrayList<>();

	protected ReadOnlyDoubleWrapper unitQuantityProperty;
	@JsonIgnore public ReadOnlyDoubleProperty getUnitQuantityProperty() { return unitQuantityProperty.getReadOnlyProperty(); }

	protected ReadOnlyDoubleWrapper totalCostProperty;
	@JsonIgnore public ReadOnlyDoubleProperty getTotalCostProperty() { return totalCostProperty.getReadOnlyProperty(); }
	@JsonIgnore public double getTotalCost() { return totalCostProperty.get(); }
	protected ReadOnlyIntegerWrapper marginProperty;
	@JsonIgnore public int getMargin() { return marginProperty.get(); }
	@JsonIgnore public ReadOnlyIntegerProperty getMarginProperty() { return marginProperty.getReadOnlyProperty(); }
	
	@JsonIgnore protected ReadOnlyDoubleWrapper totalQuantityProperty;
	@JsonIgnore public ReadOnlyDoubleProperty getTotalQuantityProperty() { return totalQuantityProperty.getReadOnlyProperty(); }
	@JsonIgnore public double getTotalQuantity() { return totalQuantityProperty.get(); }
	
	protected DoubleBinding marginDenominatorProperty;
	@JsonIgnore @Getter protected DoubleBinding totalPriceProperty;
	@JsonIgnore @Getter protected DoubleBinding unitCostProperty;
	@JsonIgnore @Getter protected DoubleBinding unitPriceProperty;
	
	@JsonIgnore @Getter protected ObjectProperty<Build> buildProperty = new SimpleObjectProperty<>();
	@JsonIgnore public void setParentBuild(Build build) { buildProperty.set(build); }
	@JsonIgnore public Build getParentBuild() { return buildProperty.get(); }
	
	public BOMEntry(String id, String shortName, String longName, String unitOfMeasure, double costPerQuantity, boolean customEntry, double quantity, int margin, boolean ignoreParentQuantity, Build parent)
	{
		this.buildProperty.set(parent);
		
		this.id = id;
		this.shortName = shortName;
		this.longName = longName;
		this.unitOfMeasure = unitOfMeasure;
		this.costPerQuantityProperty = new SimpleDoubleProperty(costPerQuantity);
		this.customEntry = new ReadOnlyBooleanWrapper(customEntry);
		
		this.unoverriddenQuantityProperty = new SimpleDoubleProperty(quantity);
		this.overridenQuantityProperty = new SimpleDoubleProperty(-1.0);
		this.quantityOverriddenProperty = overridenQuantityProperty.greaterThanOrEqualTo(0.0).and(this.customEntry.not()).and(overridenQuantityProperty.isEqualTo(unoverriddenQuantityProperty).not());

		this.unoverriddenMarginProperty = new SimpleIntegerProperty(margin);
		this.overridenMarginProperty = new SimpleIntegerProperty(-1);
		this.marginOverriddenProperty = overridenMarginProperty.greaterThanOrEqualTo(0.0);
		
		this.ignoreParentQuantityProperty = new SimpleBooleanProperty(ignoreParentQuantity);
		
		unitQuantityProperty = new ReadOnlyDoubleWrapper();
		unitQuantityProperty.bind(Bindings.when(quantityOverriddenProperty).then(overridenQuantityProperty).otherwise(unoverriddenQuantityProperty));
		
		this.unitCostProperty = unitQuantityProperty.multiply(costPerQuantityProperty);
		
		totalQuantityProperty = new ReadOnlyDoubleWrapper();
		MonadicBinding<Number> safeParentQtyBinding = EasyBind.select(buildProperty).selectObject(b -> b.getQuantity());
		totalQuantityProperty.bind(Bindings.when(ignoreParentQuantityProperty).then(unitQuantityProperty).otherwise(unitQuantityProperty.multiply(
			Bindings.createDoubleBinding(() -> safeParentQtyBinding.get() == null ? 0 : safeParentQtyBinding.get().doubleValue(), safeParentQtyBinding))));
		
		this.totalCostProperty = new ReadOnlyDoubleWrapper();
		totalCostProperty.bind(Bindings.when(ignoreParentQuantityProperty).then(unitCostProperty).otherwise(costPerQuantityProperty.multiply(totalQuantityProperty)));
		
		this.marginProperty = new ReadOnlyIntegerWrapper();
		marginProperty.bind(Bindings.when(marginOverriddenProperty).then(overridenMarginProperty).otherwise(unoverriddenMarginProperty));
		
		this.marginDenominatorProperty = Bindings.createDoubleBinding(() -> 1.0).subtract(marginProperty.getReadOnlyProperty().divide(100.0));
		this.totalPriceProperty = totalCostProperty.getReadOnlyProperty().divide(marginDenominatorProperty);
		this.unitPriceProperty = unitCostProperty.divide(marginDenominatorProperty);
	}
	
	public BOMEntry(String id, boolean custom, boolean ignoreParentQuantity, double qty, double overriddenQty, int margin, int overriddenMargin)
	{
		this(id, "", "", "", 0, custom, qty, margin, ignoreParentQuantity, null);
		this.overridenQuantityProperty.set(overriddenQty);
		this.overridenMarginProperty.set(overriddenMargin);
	}
	
	public BOMEntry(String id, String shortName, String longName, String unitOfMeasure, double costPerQuantity, boolean custom, int margin, boolean ignoreParentQuantity, Build parent)
	{
		this(id, shortName, longName, unitOfMeasure, costPerQuantity, custom, 0.0, margin, ignoreParentQuantity, parent);
	}

	public BOMEntry duplicate(double newCostPerQuantity, double newQuantity, Build parent, boolean isCustom, boolean ignoreParentQuantity, int newMargin)
	{
		BOMEntry newEntry = new BOMEntry(id, shortName, longName, unitOfMeasure, newCostPerQuantity, isCustom, newQuantity, newMargin, ignoreParentQuantity, parent);
		newEntry.putStoredValues(getStoredValues());
		Registry.handleBOMDuplicate(newEntry);
		return newEntry;
	}

	public BOMEntry duplicate(double newCostPerQuantity, double newQuantity, Build parent, boolean isCustom, boolean ignoreParentQuantity)
	{ return duplicate(newCostPerQuantity, newQuantity, parent, isCustom, ignoreParentQuantity, unoverriddenMarginProperty.get());}
	public BOMEntry duplicate(double newCostPerQuantity, double newQuantity, Build parent, boolean isCustom) { return duplicate(newCostPerQuantity, newQuantity, parent, isCustom, getIgnoreParentQuantity()); }
	public BOMEntry duplicate(double newQuantity, Build parent, boolean isCustom, boolean ignoreParentQuantity) { return duplicate(getCostPerQuantity(), newQuantity, parent, isCustom, ignoreParentQuantity); }
	public BOMEntry duplicate(double newQuantity, Build parent, boolean isCustom) { return duplicate(newQuantity, parent, isCustom, getIgnoreParentQuantity()); }
	public BOMEntry duplicate(double newQuantity, Build parent) { return duplicate(newQuantity, parent, isCustomEntry()); }
	public BOMEntry duplicate(Build parent) { return duplicate(getQuantity(), parent); }
	
	@JsonIgnore @Override public String getSearchName() { return "BOM Entry - " + shortName; }
	@JsonIgnore @Override public Object getSummaryValue() { return this; }
}
