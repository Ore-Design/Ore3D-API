package design.ore.Ore3DAPI.data.pricing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import design.ore.Ore3DAPI.Registry;
import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.Util.Mapper;
import design.ore.Ore3DAPI.data.interfaces.ValueStorageRecord;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableNumberValue;
import lombok.Getter;

@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class MiscEntry extends ValueStorageRecord
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

	public MiscEntry() { this("", 0, 1, Util.zeroDoubleBinding()); }
	public MiscEntry(String name, double cost, double quantity, ObservableNumberValue parentQuantity)
	{
		nameProperty = new SimpleStringProperty(name);
		priceProperty = new SimpleDoubleProperty(cost);
		quantityProperty = new SimpleDoubleProperty(quantity);
		unitPriceProperty = quantityProperty.multiply(priceProperty);
		ignoreParentQuantityProperty = new SimpleBooleanProperty(false);
		unitOfMeasureProperty = new SimpleStringProperty("");
		
		rebind(parentQuantity);
	}
	
	public void rebind(ObservableNumberValue parentQuantity)
	{ totalPriceProperty.bind(Bindings.when(ignoreParentQuantityProperty).then(unitPriceProperty).otherwise((unitPriceProperty.multiply(parentQuantity)))); }

	public MiscEntry duplicate(ObservableNumberValue newParentQuantity)
	{
		MiscEntry newEntry = null;
		try
		{
			String json = Mapper.getMapper().writeValueAsString(this);
			newEntry = Mapper.getMapper().readValue(json, MiscEntry.class);
			newEntry.rebind(newParentQuantity);
			Registry.handleMiscDuplicate(newEntry);
		}
		catch (JsonProcessingException e) 
		{
			Log.getLogger().error("Issue duplicating Misc entry!\n" + Util.stackTraceArrayToString(e));
		}
		
		return newEntry;
	}
}
