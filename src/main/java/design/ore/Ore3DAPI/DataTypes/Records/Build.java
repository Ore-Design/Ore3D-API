package design.ore.Ore3DAPI.DataTypes.Records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.DataTypes.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Conflict;
import design.ore.Ore3DAPI.DataTypes.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Tag;
import design.ore.Ore3DAPI.DataTypes.Interfaces.Conflictable;
import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.Spec;
import design.ore.Ore3DAPI.Jackson.ObservableListSerialization;
import design.ore.Ore3DAPI.Jackson.PropertySerialization;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
public abstract class Build extends ValueStorageRecord implements Conflictable
{
	@Getter protected UUID buildUUID = UUID.randomUUID();
	
	// TODO: Children
	//@Getter protected List<UUID> children = new ArrayList<>();
	
	@Getter protected PositiveIntSpec quantity;

	@JsonIgnore @Getter protected BuildPrice price;

	@JsonSerialize(using = PropertySerialization.StringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.StringSer.Deserializer.class)
	@Getter protected SimpleStringProperty titleProperty = new SimpleStringProperty("Build");
	
	// TODO: Description
	@Getter String description;
	boolean descriptionIsOverriden;

	@JsonSerialize(using = ObservableListSerialization.TagList.Serializer.class)
	@JsonDeserialize(using = ObservableListSerialization.TagList.Deserializer.class)
	@Getter
	protected ObservableList<Tag> tags = FXCollections.observableArrayList();

	@JsonSerialize(using = ObservableListSerialization.BOMEntryList.Serializer.class)
	@JsonDeserialize(using = ObservableListSerialization.BOMEntryList.Deserializer.class)
	@Getter
	protected ObservableList<BOMEntry> bom = FXCollections.observableArrayList();
	
	@JsonSerialize(using = ObservableListSerialization.RoutingEntryList.Serializer.class)
	@JsonDeserialize(using = ObservableListSerialization.RoutingEntryList.Deserializer.class)
	@Getter
	protected ObservableList<RoutingEntry> routings = FXCollections.observableArrayList();

	@JsonSerialize(using = ObservableListSerialization.ConflictList.Serializer.class)
	@JsonDeserialize(using = ObservableListSerialization.ConflictList.Deserializer.class)
	@Getter
	protected ObservableList<Conflict> conflicts;
	
	@JsonIgnore protected Logger LOG;
	
	public Build()
	{
		this(null);
	}
	
	public Build(Logger log)
	{
		this.LOG = log;
		conflicts = FXCollections.observableArrayList();
		
		initializeSpecs();
		
		resetSpecListeners();
		refresh();
	}
	
	public void resetSpecListeners()
	{
		this.price = new BuildPrice(this);
		for(Spec<?> s : getSpecs()) { s.getProperty().addListener((obsv, oldVal, newVal) -> refresh()); }
	}

	public abstract Build duplicate();
	public abstract List<BOMEntry> calculateStandardBOMs();
	public abstract List<RoutingEntry> calculateRoutings();
	
	public abstract String calculateDefaultDescription();
	public abstract void runCalculations();
	protected void initializeSpecs()
	{
		quantity = new PositiveIntSpec("Quantity", 1, false, "Overview");
	}

	protected abstract DoubleBinding getAdditionalPriceModifiers();	
	public abstract String buildTypeID();
	@JsonIgnore public List<Spec<?>> getSpecs() { return new ArrayList<>(Arrays.asList(quantity)); }
	protected DoubleBinding getUnitPrice()
	{
		DoubleBinding binding = null;
		
		DoubleBinding additionalMods = getAdditionalPriceModifiers();
		if(additionalMods != null) binding = additionalMods.add(0);
		
		for(BOMEntry bomEntry : bom)
		{
			if(binding == null ) binding = bomEntry.getUnitPriceProperty().add(0);
			else binding = binding.add(bomEntry.getUnitPriceProperty());
		}
		for(RoutingEntry routingEntry : routings)
		{
			if(binding == null ) binding = routingEntry.getTotalPriceProperty().add(0);
			else binding = binding.add(routingEntry.getTotalPriceProperty());
		}
		
		return binding == null ? new ReadOnlyDoubleWrapper(0).add(0) : binding;
	}
	
	private void refresh()
	{
		conflicts.clear();

		Map<String, Pair<Double, Integer>> overriddenStandardBOMS = new HashMap<>();
		
		List<BOMEntry> bomToRemove = new ArrayList<>();
		for(BOMEntry e : bom)
		{
			if(!e.getCustomEntryProperty().get())
			{
				Double quantityOverride = null;
				Integer marginOverride = null;
				if(e.getQuantityOverriddenProperty().get()) quantityOverride = e.getOverridenQuantityProperty().get();
				if(e.getMarginOverriddenProperty().get()) marginOverride = e.getOverridenMarginProperty().get();
				
				if(quantityOverride != null || marginOverride != null) overriddenStandardBOMS.put(e.getId(), new Pair<Double, Integer>(quantityOverride, marginOverride));
				
				bomToRemove.add(e);
			}
		}
		
		Map<String, Double> overriddenRoutings = new HashMap<>();
		
		List<RoutingEntry> routingsToRemove = new ArrayList<>();
		for(RoutingEntry e : routings)
		{
				Double quantityOverride = null;
				if(e.getQuantityOverriddenProperty().get()) quantityOverride = e.getOverridenQuantityProperty().get();
				
				if(quantityOverride != null) overriddenRoutings.put(e.getId(), quantityOverride);
				
				routingsToRemove.add(e);
		}
		
		bom.removeAll(bomToRemove);
		routings.removeAll(routingsToRemove);
		
		for(BOMEntry e : calculateStandardBOMs())
		{
			if(overriddenStandardBOMS.containsKey(e.getId()))
			{
				Pair<Double, Integer> overrides = overriddenStandardBOMS.get(e.getId());
				Double qtyOverride = overrides.getKey();
				Integer marginOverride = overrides.getValue();
				if(qtyOverride != null) e.getOverridenQuantityProperty().set(qtyOverride);
				if(marginOverride != null) e.getOverridenMarginProperty().set(marginOverride);
			}
			
			bom.add(e);
		}
		
		for(RoutingEntry r : calculateRoutings())
		{
			if(overriddenRoutings.containsKey(r.getId()))
			{
				Double overrides = overriddenRoutings.get(r.getId());
				if(overrides != null) r.getOverridenQuantityProperty().set(overrides);
			}
			
			routings.add(r);
		}
	}
	
	protected DoubleBinding getTotalPrice()
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

	@Override
	public void addConflict(Conflict conflict) { conflicts.add(conflict); }

	@Override
	public void clearConflicts() { conflicts.clear(); }
}
