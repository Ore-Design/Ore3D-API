package design.ore.Ore3DAPI.DataTypes.Records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import design.ore.Ore3DAPI.DataTypes.BOMComponent;
import design.ore.Ore3DAPI.DataTypes.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Description;
import design.ore.Ore3DAPI.DataTypes.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Tag;
import design.ore.Ore3DAPI.DataTypes.Pricing.BuildPrice;
import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.Spec;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@AllArgsConstructor
@NoArgsConstructor
public abstract class Build extends ValueStorageRecord
{
	@Getter protected UUID buildUUID = UUID.randomUUID();
	@Getter protected List<UUID> children = new ArrayList<>();
	
	@Getter protected Spec<Integer> quantity = new PositiveIntSpec("Quantity", 1, false, "Overview");
	
	@Getter protected BuildPrice price = new BuildPrice(this, getUnitPrice());
	
	@Getter protected SimpleStringProperty titleProperty = new SimpleStringProperty("Build");
	
	@Getter protected Description description = new Description();
	
	@Getter protected List<Tag> tags = new ArrayList<>();
	
	@Getter protected List<BOMEntry> bom = new ArrayList<>();
	@Getter protected List<RoutingEntry> routings = new ArrayList<>();
	
	public abstract String calculateDefaultDescription();
	public abstract void runCalculations();
	protected abstract DoubleBinding getAdditionalPriceModifiers();
	public abstract String id();
	public List<Spec<?>> getSpecs() { return new ArrayList<>(Arrays.asList(quantity)); }
	private DoubleBinding getUnitPrice()
	{
		DoubleBinding binding = null;
		
		binding = getAdditionalPriceModifiers().add(0);
		
		for(BOMEntry bomEntry : bom) { binding = binding.add(bomEntry.getTotalPriceProperty()); }
		for(RoutingEntry routingEntry : routings) { binding = binding.add(routingEntry.getTotalPriceProperty()); }
		
		return binding;
	}
}
