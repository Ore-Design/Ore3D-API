package design.ore.api.ore3d.data.pricing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import design.ore.api.ore3d.Registry;
import design.ore.api.ore3d.Util;
import design.ore.api.ore3d.data.core.Build;
import design.ore.api.ore3d.data.interfaces.ISummaryOption;
import design.ore.api.ore3d.data.interfaces.ValueStorageRecord;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import lombok.Getter;
import lombok.Setter;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class RoutingEntry extends ValueStorageRecord implements ISummaryOption
{
	public RoutingEntry() { this("", "", 0, 0, 0, null, false); }
	
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

	protected final ReadOnlyDoubleWrapper unitQuantityProperty;
	@JsonIgnore public ReadOnlyDoubleProperty getQuantityProperty() { return unitQuantityProperty.getReadOnlyProperty(); }
	protected final ReadOnlyDoubleWrapper totalCostProperty;
	@JsonIgnore public ReadOnlyDoubleProperty getTotalCostProperty() { return totalCostProperty.getReadOnlyProperty(); }
	protected final ReadOnlyIntegerWrapper marginProperty;
	@JsonProperty("m") public int getMargin() { return marginProperty.get(); }
	@JsonProperty("m") public void setMargin(int val)
	{
		if(name.equalsIgnoreCase("Fab") && val == 56)
		{
			Util.Log.getLogger().info("Margin on routing {} of build {} set to {}!", name, (getParentBuild() == null ? "UNKNOWN" : getParentBuild().getTitleProperty().get()), val, new Exception("Stack trace"));
		}
		marginProperty.set(val);
	}
	@JsonIgnore public ReadOnlyIntegerProperty getMarginProperty() { return marginProperty.getReadOnlyProperty(); }

	@JsonIgnore protected ReadOnlyDoubleWrapper totalQuantityProperty;
	@JsonIgnore public ReadOnlyDoubleProperty getTotalQuantityProperty() { return totalQuantityProperty.getReadOnlyProperty(); }
	@JsonIgnore public double getTotalQuantity() { return totalQuantityProperty.get(); }
	
	protected DoubleBinding marginDenominatorProperty;
	@JsonIgnore @Getter protected DoubleBinding totalPriceProperty;
	@JsonIgnore @Getter protected DoubleBinding unitCostProperty;
	@JsonIgnore @Getter protected DoubleBinding unitPriceProperty;
	
	@JsonIgnore @Getter protected final ObjectProperty<Build> buildProperty = new SimpleObjectProperty<>();
	@JsonIgnore public void setParentBuild(Build build) { buildProperty.set(build); }
	@JsonIgnore public Build getParentBuild() { return buildProperty.get(); }
	
	public RoutingEntry(String id, String name, double costPerQuantity, double quantity, int margin, Build parent, boolean customEntry)
	{
		buildProperty.set(parent);
		
		this.id = id;
		this.name = name;
		this.costPerQuantityProperty = new SimpleDoubleProperty();
		this.customEntry = new ReadOnlyBooleanWrapper(customEntry);
		
		this.unoverriddenQuantityProperty = new SimpleDoubleProperty(quantity);
		this.overridenQuantityProperty = new SimpleDoubleProperty(-1.0);
		this.quantityOverriddenProperty = overridenQuantityProperty.greaterThanOrEqualTo(0.0).and(this.customEntry.not()).and(overridenQuantityProperty.isEqualTo(unoverriddenQuantityProperty).not());
		
		unitQuantityProperty = new ReadOnlyDoubleWrapper();
		unitQuantityProperty.bind(Bindings.when(quantityOverriddenProperty).then(overridenQuantityProperty).otherwise(unoverriddenQuantityProperty));
		
		this.costPerQuantityProperty.setValue(costPerQuantity);
		this.unitCostProperty = unitQuantityProperty.multiply(costPerQuantityProperty);
		
		this.totalCostProperty = new ReadOnlyDoubleWrapper();

		MonadicBinding<Number> safeParentQtyBinding = EasyBind.select(buildProperty).selectObject(b -> b.getQuantity());
		totalCostProperty.bind(unitCostProperty.multiply(Bindings.createDoubleBinding(() -> safeParentQtyBinding.get() == null ? 0 : safeParentQtyBinding.get().doubleValue(), safeParentQtyBinding)));

		totalQuantityProperty = new ReadOnlyDoubleWrapper();
		totalQuantityProperty.bind(unitQuantityProperty.multiply(Bindings.createDoubleBinding(() -> safeParentQtyBinding.get() == null ? 0 : safeParentQtyBinding.get().doubleValue(), safeParentQtyBinding)));
		
		this.marginProperty = new ReadOnlyIntegerWrapper(margin);
		
		this.marginDenominatorProperty = new ReadOnlyDoubleWrapper(1.0).subtract(marginProperty.getReadOnlyProperty().divide(100.0));
		this.totalPriceProperty = totalCostProperty.getReadOnlyProperty().divide(marginDenominatorProperty);
		this.unitPriceProperty = unitCostProperty.divide(marginDenominatorProperty);
	}
	
	public RoutingEntry(String id, String name, double costPerQuantity, int margin, Build parent, boolean customEntry)
	{
		this(id, name, costPerQuantity, 0.0, margin, parent, customEntry);
	}

	public RoutingEntry duplicate(double newCostPerQuantity, double newQuantity, Build parent, int margin, Double overriddenQuantity, boolean isCustom)
	{
		RoutingEntry newEntry = new RoutingEntry(id, name, newCostPerQuantity, newQuantity, margin, parent, isCustom);
		newEntry.putStoredValues(getStoredValues());
		if(overriddenQuantity != null) newEntry.setOverriddenQuantity(overriddenQuantity);
		Registry.handleRoutingDuplicate(newEntry);
		return newEntry;
	}
	public RoutingEntry duplicate(double newQuantity, Build parent, Double overriddenQuantity, boolean isCustom) { return duplicate(costPerQuantityProperty.doubleValue(), newQuantity, parent, getMargin(), overriddenQuantity, isCustom); }
	public RoutingEntry duplicate(double newCostPerQuantity, double newQuantity, Build parent, Double overriddenQuantity, boolean isCustom) { return duplicate(newCostPerQuantity, newQuantity, parent, getMargin(), overriddenQuantity, isCustom); }
	public RoutingEntry duplicate(double newCostPerQuantity, double newQuantity, Build parent, boolean isCustom) { return duplicate(newCostPerQuantity, newQuantity, parent, null, isCustom); }
	public RoutingEntry duplicate(double newQuantity, Build parent, boolean isCustom) { return duplicate(getCostPerQuantity(), newQuantity, parent, isCustom); }
	public RoutingEntry duplicate(double newQuantity, Build parent) { return duplicate(newQuantity, parent, isCustomEntry()); }
	public RoutingEntry duplicate(Build parent) { return duplicate(getQuantity(), parent); }
	
	@JsonIgnore @Override public String getSearchName() { return "Routing Entry - " + name; }
	@JsonIgnore @Override public Object getSummaryValue() { return this; }
}
