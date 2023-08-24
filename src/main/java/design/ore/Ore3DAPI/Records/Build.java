package design.ore.Ore3DAPI.Records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import design.ore.Ore3DAPI.Records.Subtypes.BOMComponent;
import design.ore.Ore3DAPI.Records.Subtypes.Description;
import design.ore.Ore3DAPI.Records.Subtypes.Routing;
import design.ore.Ore3DAPI.Records.Subtypes.Tag;
import design.ore.Ore3DAPI.Records.Subtypes.Pricing.BuildPrice;
import design.ore.Ore3DAPI.Records.Subtypes.Specs.PositiveIntSpec;
import design.ore.Ore3DAPI.Records.Subtypes.Specs.Spec;
import javafx.beans.property.SimpleStringProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@AllArgsConstructor
@NoArgsConstructor
public abstract class Build
{
	@Getter UUID buildUUID = UUID.randomUUID();
	@Getter List<UUID> children = new ArrayList<>();
	
	@Getter Spec<Integer> quantity = new PositiveIntSpec("Quantity", 1, false, "Overview");
	
	@Getter BuildPrice price = new BuildPrice(this);
	
	@Getter SimpleStringProperty titleProperty = new SimpleStringProperty("Build");
	
	@Getter Description description = new Description();
	
	@Getter List<Tag> tags = new ArrayList<>();
	
	@Getter List<BOMComponent> bom = new ArrayList<>();
	@Getter List<Routing> routings = new ArrayList<>();
	
	public abstract String calculateDefaultDescription();
	public abstract void runCalculations();
	public List<Spec<?>> getSpecs() { return new ArrayList<>(Arrays.asList(quantity)); }
}
