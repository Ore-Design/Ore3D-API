package design.ore.Ore3DAPI.DataTypes.Records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonFormat;

import design.ore.Ore3DAPI.DataTypes.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Description;
import design.ore.Ore3DAPI.DataTypes.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Tag;
import design.ore.Ore3DAPI.DataTypes.Pricing.BuildPrice;
import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.Spec;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public abstract class Build extends ValueStorageRecord
{
	@Getter protected UUID buildUUID = UUID.randomUUID();
	@Getter protected List<UUID> children = new ArrayList<>();
	
	@Getter protected Spec<Integer> quantity = new PositiveIntSpec("Quantity", 1, false, "Overview");

	@Getter protected BuildPrice price;
	
	@Getter protected SimpleStringProperty titleProperty = new SimpleStringProperty("Build");
	
	@Getter protected Description description = new Description();
	
	@Getter protected List<Tag> tags = new ArrayList<>();
	
	@Getter protected ObservableList<BOMEntry> bom = FXCollections.observableArrayList();
	@Getter protected ObservableList<RoutingEntry> routings = FXCollections.observableArrayList();
	
	protected Logger LOG;
	
	public Build()
	{
		this.price = new BuildPrice(this);
	}
	
	public Build(Logger log)
	{
		this.LOG = log;
		this.price = new BuildPrice(this);
	}
	
	public abstract Build duplicate();
	
	public abstract String calculateDefaultDescription();
	public abstract void runCalculations();

	protected abstract DoubleBinding getAdditionalPriceModifiers();	
	public abstract String id();
	public List<Spec<?>> getSpecs() { return new ArrayList<>(Arrays.asList(quantity)); }
	public DoubleBinding getUnitPrice()
	{
		DoubleBinding binding = null;
		
		DoubleBinding additionalMods = getAdditionalPriceModifiers();
		if(additionalMods != null) binding = additionalMods.add(0);
		
		for(BOMEntry bomEntry : bom)
		{
			if(binding == null ) binding = bomEntry.getUnitPriceProperty().add(0);
			else binding = binding.add(bomEntry.getUnitPriceProperty());
		}
		// TODO: Unit price for routings
//		for(RoutingEntry routingEntry : routings)
//		{
//			if(binding == null ) binding = routingEntry.getTotalPriceProperty().add(0);
//			else binding = binding.add(routingEntry.getTotalPriceProperty());
//		}

		if(binding != null) debug("Final binding value is now " + binding.get());
		else warn("Final binding is null!");
		
		return binding == null ? new ReadOnlyDoubleWrapper(0).add(0) : binding;
	}
	public DoubleBinding getTotalPrice()
	{
		DoubleBinding binding = null;
		
		DoubleBinding additionalMods = getAdditionalPriceModifiers();
		if(additionalMods != null) binding = additionalMods.multiply(((PositiveIntSpec)this.quantity).getNumberProperty());
		
		for(BOMEntry bomEntry : bom)
		{
			if(binding == null ) binding = bomEntry.getTotalPriceProperty().add(0);
			else binding = binding.add(bomEntry.getTotalPriceProperty());
		}
		for(RoutingEntry routingEntry : routings)
		{
			if(binding == null ) binding = routingEntry.getTotalPriceProperty().add(0);
			else binding = binding.add(routingEntry.getTotalPriceProperty());
		}

		if(binding != null) debug("Final binding value is now " + binding.get());
		else warn("Final binding is null!");
		
		return binding == null ? new ReadOnlyDoubleWrapper(0).add(0) : binding;
	}
	
	protected void debug(String message)
	{
		if(this.LOG != null) LOG.debug(message);
		else System.out.println(message);
	}
	
	protected void info(String message)
	{
		if(this.LOG != null) LOG.info(message);
		else System.out.println(message);
	}
	
	protected void warn(String message)
	{
		if(this.LOG != null) LOG.debug(message);
		else System.out.println("WARN: " + message);
	}
	
	protected void error(String message)
	{
		if(this.LOG != null) LOG.debug(message);
		else System.err.println(message);
	}
}
