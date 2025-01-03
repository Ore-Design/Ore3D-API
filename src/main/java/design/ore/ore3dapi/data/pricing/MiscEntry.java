package design.ore.ore3dapi.data.pricing;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import design.ore.ore3dapi.Registry;
import design.ore.ore3dapi.Util;
import design.ore.ore3dapi.Util.Log;
import design.ore.ore3dapi.Util.Mapper;
import design.ore.ore3dapi.data.core.Build;
import design.ore.ore3dapi.data.interfaces.ISummaryOption;
import design.ore.ore3dapi.data.interfaces.ValueStorageRecord;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class MiscEntry extends ValueStorageRecord implements ISummaryOption
{
	@JsonIgnore @Getter protected final SimpleStringProperty nameProperty;
	@JsonProperty("n") public String getName() { return nameProperty.get(); }
	@JsonProperty("n") public void setName(String val) { nameProperty.set(val); }
	@JsonIgnore @Getter protected final SimpleDoubleProperty priceProperty;
	@JsonProperty("c") public double getPrice() { return priceProperty.get(); }
	@JsonProperty("c") public void setPrice(double val) { priceProperty.set(val); }

	@JsonIgnore @Getter protected final SimpleDoubleProperty quantityProperty;
	@JsonProperty("q") public double getQuantity() { return quantityProperty.get(); }
	@JsonProperty("q") public void setQuantity(double val) { quantityProperty.set(val); }
	
	@JsonIgnore protected ReadOnlyDoubleWrapper totalPriceProperty = new ReadOnlyDoubleWrapper();
	@JsonIgnore public ReadOnlyDoubleProperty getTotalPriceProperty() { return totalPriceProperty.getReadOnlyProperty(); }
	@JsonIgnore @Getter protected final DoubleBinding unitPriceProperty;
	
	@JsonIgnore @Getter protected SimpleBooleanProperty ignoreParentQuantityProperty;
	@JsonProperty("ipq") public boolean getIgnoreParentQuantity() { return ignoreParentQuantityProperty.get(); }
	@JsonProperty("ipq") public void setIgnoreParentQuantity(boolean val) { ignoreParentQuantityProperty.set(val); }
	
	@JsonIgnore @Getter protected SimpleStringProperty unitOfMeasureProperty;
	@JsonProperty("uom") public String getUnitOfMeasure() { return unitOfMeasureProperty.get(); }
	@JsonProperty("uom") public void setUnitOfMeasure(String val) { unitOfMeasureProperty.set(val); }
	
	@JsonIgnore @Getter protected final ObjectProperty<Build> buildProperty = new SimpleObjectProperty<>();
	public void setParentBuild(Build build) { buildProperty.set(build); }
	public Build getParentBuild() { return buildProperty.get(); }

	public MiscEntry() { this("", 0, 1, null); }
	public MiscEntry(String name, double cost, double quantity, Build parent)
	{
		buildProperty.set(parent);
		
		nameProperty = new SimpleStringProperty(name);
		priceProperty = new SimpleDoubleProperty(cost);
		quantityProperty = new SimpleDoubleProperty(quantity);
		unitPriceProperty = quantityProperty.multiply(priceProperty);
		ignoreParentQuantityProperty = new SimpleBooleanProperty(false);
		unitOfMeasureProperty = new SimpleStringProperty("");

		MonadicBinding<Number> safeParentQtyBinding = EasyBind.select(buildProperty).selectObject(b -> b.getQuantity());
		totalPriceProperty.bind(Bindings.when(ignoreParentQuantityProperty).then(unitPriceProperty)
			.otherwise(unitPriceProperty.multiply(Bindings.createDoubleBinding(() -> safeParentQtyBinding.get() == null ? 0 : safeParentQtyBinding.get().doubleValue(), safeParentQtyBinding))));
	}
	public MiscEntry duplicate(Build newParent)
	{
		MiscEntry newEntry = null;
		try
		{
			String json = Mapper.getMapper().writeValueAsString(this);
			newEntry = Mapper.getMapper().readValue(json, MiscEntry.class);
			newEntry.setParentBuild(newParent);
			Registry.handleMiscDuplicate(newEntry);
		}
		catch (JsonProcessingException e) 
		{
			Log.getLogger().error("Issue duplicating Misc entry!\n" + Util.throwableToString(e));
		}
		
		return newEntry;
	}
	
	@JsonIgnore @Override public String getSearchName() { return "Misc Entry - " + getName(); }
	@JsonIgnore @Override public Object getSummaryValue() { return this; }
}
