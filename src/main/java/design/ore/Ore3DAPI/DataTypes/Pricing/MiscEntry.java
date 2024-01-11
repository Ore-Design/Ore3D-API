package design.ore.Ore3DAPI.DataTypes.Pricing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.Util.Mapper;
import design.ore.Ore3DAPI.DataTypes.Interfaces.ValueStorageRecord;
import design.ore.Ore3DAPI.Jackson.ComponentSerialization;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableNumberValue;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonSerialize(using = ComponentSerialization.Misc.Serializer.class)
@JsonDeserialize(using = ComponentSerialization.Misc.Deserializer.class)
public class MiscEntry extends ValueStorageRecord
{
	@Getter protected final SimpleStringProperty nameProperty;
	public String getName() { return nameProperty.get(); }
	@Getter protected final SimpleDoubleProperty priceProperty;
	public double getPrice() { return priceProperty.get(); }

	@Getter protected final SimpleDoubleProperty quantityProperty;
	@Getter protected DoubleBinding totalPriceProperty;
	@Getter protected final DoubleBinding unitPriceProperty;
	
	public MiscEntry(String name, double cost, double quantity, ObservableNumberValue parentQuantity)
	{
		nameProperty = new SimpleStringProperty(name);
		priceProperty = new SimpleDoubleProperty(cost);
		quantityProperty = new SimpleDoubleProperty(quantity);
		
		unitPriceProperty = quantityProperty.multiply(priceProperty);
		totalPriceProperty = unitPriceProperty.multiply(parentQuantity);
	}

	public MiscEntry duplicate(ObservableNumberValue newParentQuantity)
	{
		MiscEntry newEntry = null;
		try
		{
			String json = Mapper.getMapper().writeValueAsString(this);
			newEntry = Mapper.getMapper().readValue(json, MiscEntry.class);
			newEntry.totalPriceProperty = newEntry.unitPriceProperty.multiply(newParentQuantity);
		}
		catch (JsonProcessingException e) 
		{
			Log.getLogger().error("Issue duplicating Misc entry!\n" + Util.stackTraceArrayToString(e));
		}
		
		return newEntry;
	}
}
