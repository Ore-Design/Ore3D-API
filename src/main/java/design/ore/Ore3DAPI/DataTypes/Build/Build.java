package design.ore.Ore3DAPI.DataTypes.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.DataTypes.Conflict;
import design.ore.Ore3DAPI.DataTypes.CRM.Transaction;
import design.ore.Ore3DAPI.DataTypes.Interfaces.Conflictable;
import design.ore.Ore3DAPI.DataTypes.Interfaces.ValueStorageRecord;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.Spec;
import design.ore.Ore3DAPI.DataTypes.Specs.StringSpec;
import design.ore.Ore3DAPI.Jackson.ObservableListSerialization;
import design.ore.Ore3DAPI.Jackson.ObservableSetSerialization;
import design.ore.Ore3DAPI.Jackson.PropertySerialization;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.util.Pair;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
public abstract class Build extends ValueStorageRecord implements Conflictable
{
	private final ChangeListener<Boolean> childUpdateListener = new ChangeListener<Boolean>()
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) { if(newVal) { buildIsDirty.setValue(true); } }
	};
	
	@Getter protected int buildUUID = new Random().nextInt(111111, 1000000);
	public void regenerateBuildUUID() { buildUUID = new Random().nextInt(111111, 1000000); }
	
	@JsonMerge @Getter protected PositiveIntSpec quantity = new PositiveIntSpec("Quantity", 1, false, "Overview", false);
	@JsonMerge @Getter protected Spec<String> workOrder = new StringSpec("Work Order", "", true, null, false);

	@JsonSerialize(using = PropertySerialization.DoubleSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.DoubleSer.Deserializer.class)
	@Getter protected DoubleProperty catalogPrice = new SimpleDoubleProperty(-1);
	
	@JsonIgnore @Getter protected ObjectProperty<Transaction> parentTransactionProperty = new SimpleObjectProperty<Transaction>();
	
	protected SimpleBooleanProperty buildIsDirty = new SimpleBooleanProperty(false);
	public void setDirty() { buildIsDirty.setValue(true); }
	
	private Map<String, ChangeListener<Boolean>> registeredDirtyUpdates = new HashMap<>();
	public void registerDirtyListenerEvent(String listenerID, ChangeListener<Boolean> listener)
	{
		if(registeredDirtyUpdates.containsKey(listenerID)) Util.Log.getLogger().warn("Attempted to add listener " + listenerID + " to build, but its already registered!");
		else registeredDirtyUpdates.put(listenerID, listener);
	}
	public void unregisterDirtyListenerEvent(String listenerID) { registeredDirtyUpdates.remove(listenerID); }
	
	@JsonIgnore @Getter protected BuildPrice price;

	@JsonSerialize(using = PropertySerialization.StringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.StringSer.Deserializer.class)
	@Getter protected SimpleStringProperty titleProperty = new SimpleStringProperty("Build");
	
	// Description Stuff
	@JsonSerialize(using = PropertySerialization.StringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.StringSer.Deserializer.class)
	@JsonMerge @Getter protected SimpleStringProperty unoverridenDescriptionProperty = new SimpleStringProperty("");
	
	@JsonSerialize(using = PropertySerialization.StringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.StringSer.Deserializer.class)
	@JsonMerge @Getter protected SimpleStringProperty overridenDescriptionProperty = new SimpleStringProperty("");
	
	@JsonIgnore @Getter protected BooleanBinding descriptionIsOverridenBinding = overridenDescriptionProperty.isNotEqualTo("").and(overridenDescriptionProperty.isNotEqualTo(unoverridenDescriptionProperty));
	@JsonIgnore @Getter protected StringBinding descriptionBinding = Bindings.when(descriptionIsOverridenBinding).then(overridenDescriptionProperty).otherwise(unoverridenDescriptionProperty);
	
	@JsonIgnore protected ReadOnlyObjectWrapper<Build> parentBuildProperty = new ReadOnlyObjectWrapper<>();
	public ReadOnlyObjectProperty<Build> getParentBuildProperty() { return parentBuildProperty.getReadOnlyProperty(); }

	// We have to serialize child builds using a custom setter, otherwise linking children to parent fails.
	@JsonDeserialize(using = ObservableListSerialization.BuildList.Deserializer.class)
	@Getter protected final ObservableList<Build> childBuilds = FXCollections.observableArrayList();
	@JsonSerialize(using = ObservableListSerialization.BuildList.Serializer.class)
	private void setChildBuilds(List<Build> children) { childBuilds.clear(); childBuilds.addAll(children); }

	@JsonSerialize(using = ObservableSetSerialization.IntSet.Serializer.class)
	@JsonDeserialize(using = ObservableSetSerialization.IntSet.Deserializer.class)
	@Getter protected ObservableSet<Integer> tags;

	@JsonDeserialize(using = ObservableListSerialization.BOMEntryList.Deserializer.class)
	@Getter
	protected ObservableList<BOMEntry> bom = FXCollections.observableArrayList();
	@JsonSerialize(using = ObservableListSerialization.BOMEntryList.Serializer.class)
	private void setBOMs(List<BOMEntry> boms) { bom.clear(); bom.addAll(boms); }
	
	@JsonDeserialize(using = ObservableListSerialization.RoutingEntryList.Deserializer.class)
	@Getter
	protected ObservableList<RoutingEntry> routings = FXCollections.observableArrayList();
	@JsonSerialize(using = ObservableListSerialization.RoutingEntryList.Serializer.class)
	private void setRoutings(List<RoutingEntry> routings) { this.routings.clear(); this.routings.addAll(routings); }

	@JsonSerialize(using = ObservableListSerialization.ConflictList.Serializer.class)
	@JsonDeserialize(using = ObservableListSerialization.ConflictList.Deserializer.class)
	@Getter
	protected ObservableList<Conflict> conflicts;
	
	@JsonIgnore @Getter protected ObservableList<Spec<?>> specs;
	
	@JsonIgnore @Getter private IntegerBinding childDepth = Bindings.createIntegerBinding(() ->
		parentBuildProperty.getValue() == null ? 0 : parentBuildProperty.get().getChildDepth().get() + 1, parentBuildProperty);
	
	public Build()
	{
		this.price = new BuildPrice(this);
		
		childBuilds.addListener((ListChangeListener.Change<? extends Build> l) ->
		{
			while(l.next())
			{
				if(l.wasPermutated())
				{}
				else if(l.wasAdded())
				{
					for(Build cb : l.getAddedSubList())
					{
						if(cb.parentBuildProperty.get() != null) throw new IllegalArgumentException("The child build you are trying to add already has a parent!");
						
						cb.parentBuildProperty.setValue(this);
						cb.registerDirtyListenerEvent("ChildUpdateListener", childUpdateListener);
						this.setDirty();
					}
				}
				else if(l.wasRemoved())
				{
					for(Build cb : l.getRemoved())
					{
						cb.parentBuildProperty.setValue(null);
						cb.unregisterDirtyListenerEvent("ChildUpdateListener");
						this.setDirty();
					}
				}
			}
		});
		
		tags = FXCollections.observableSet();
		
		conflicts = FXCollections.observableArrayList();
		buildIsDirty.addListener((obs, oldVal, newVal) ->
		{
			for(Build cb : childBuilds)
			{
				if(newVal) cb.refresh();
				for(ChangeListener<Boolean> listener : cb.registeredDirtyUpdates.values()) listener.changed(obs, oldVal, newVal);
			}
			
			if(newVal) refresh();
			for(ChangeListener<Boolean> listener : registeredDirtyUpdates.values()) listener.changed(obs, oldVal, newVal);
			buildIsDirty.setValue(false);
		});
		
		specs = FXCollections.observableArrayList();
		specs.addListener((ListChangeListener.Change<? extends Spec<?>> l) ->
		{
			while(l.next())
			{
				if(l.wasAdded())
				{
					for(Spec<?> s : l.getAddedSubList())
					{
						if(!s.isReadOnly()) { s.addListener((obsv, oldVal, newVal) -> { if(oldVal == null || !oldVal.equals(newVal)) buildIsDirty.setValue(true); }); }
						if(s.getCalculateOnDirty() != null) this.registerDirtyListenerEvent(s.getCalculateOnDirty() + "CalculateOnDirty", (obs, oldVal, newVal) -> s.setPropertyToCallable());
						
						// TODO: Do we need this?
						// price.rebindPricing(this);
					}
				}
				if(l.wasRemoved()) throw new IllegalArgumentException("Removing specs from specs list is not supported!");
			}
		});
		
		specs.addAll(quantity, workOrder);
	}

	public abstract List<BOMEntry> calculateStandardBOMs();
	public abstract List<RoutingEntry> calculateRoutings();
	public abstract String calculateDefaultDescription();
	public abstract void runCalculations();
	public abstract Set<String> allowedChildClasses();
	protected abstract DoubleBinding getAdditionalPriceModifiers();	
	protected abstract String buildTypeID();
	
	public Build duplicate()
	{
		Build duplicate = null;
		try
		{
			String original = Util.Mapper.getMapper().writeValueAsString(this);
			duplicate = Util.Mapper.getMapper().readValue(original, Build.class);
		}
		catch (JsonProcessingException e) { Util.Log.getLogger().error("An error has occured while duplicating build!\n" + Util.stackTraceArrayToString(e)); }
		
		duplicate.regenerateBuildUUID();
		return duplicate;
	}
	
	@JsonIgnore public Map<Integer, Build> getChildBuildsMap()
	{
		Map<Integer, Build> allBuilds = new HashMap<>();
		for(Build cb : childBuilds)
		{
			allBuilds.put(cb.getBuildUUID(), cb);
			allBuilds.putAll(cb.getChildBuildsMap());
		}
		return allBuilds;
	}
	
	public boolean matches(Build toMatch)
	{
		if(this == toMatch) return true;
		
		if(this.buildTypeID() != toMatch.buildTypeID()) return false;
		
		for(Spec<?> s : this.specs)
		{
			if(s.countsAsMatch())
			{
				Optional<Spec<?>> optionalMatch = toMatch.specs.stream().filter(sp -> sp.getId() == s.getId()).findFirst();
				if(optionalMatch.isEmpty()) return false;
				Spec<?> matching = optionalMatch.get();
				if(!matching.getValue().equals(s.getValue())) return false;
			}
		}
		
		return true;
	}
	
	protected void refresh()
	{
		conflicts.clear();

		Map<String, Pair<Double, Integer>> overriddenStandardBOMS = new HashMap<>();
		
		this.unoverridenDescriptionProperty.setValue(this.calculateDefaultDescription());
		
		// We only clear non-custom BOMs, hence the usage of bomToRemove
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
		for(RoutingEntry e : routings)
		{
			Double quantityOverride = null;
			if(e.getQuantityOverriddenProperty().get()) quantityOverride = e.getOverridenQuantityProperty().get();
			if(quantityOverride != null) overriddenRoutings.put(e.getId(), quantityOverride);
		}
		
		bom.removeAll(bomToRemove);
		routings.clear();
		
		for(BOMEntry e : calculateStandardBOMs())
		{
			BOMEntry withPricing = e;
			if(this.parentBuildProperty.get() != null)
			{
				Build topLevelParent = this.parentBuildProperty.get();
				while(topLevelParent.parentBuildProperty.get() != null) topLevelParent = topLevelParent.parentBuildProperty.get();
				
				if(topLevelParent.parentTransactionProperty.get() != null) withPricing = Util.duplicateBOMWithPricing(topLevelParent.parentTransactionProperty.get(), this, e, e);
				else Util.Log.getLogger().warn("No transaction parent is registered for the top-level parent of build " + this.getTitleProperty().get() + ", so pricing for generated BOMs cant be matched to transaction!");
			}
			else if(parentTransactionProperty.get() != null) withPricing = Util.duplicateBOMWithPricing(parentTransactionProperty.get(), this, e, e);
			else Util.Log.getLogger().warn("No transaction parent is registered for the build " + this.getTitleProperty().get() + ", so pricing for generated BOMs cant be matched to transaction!");
				
			if(overriddenStandardBOMS.containsKey(e.getId()))
			{
				Pair<Double, Integer> overrides = overriddenStandardBOMS.get(e.getId());
				Double qtyOverride = overrides.getKey();
				Integer marginOverride = overrides.getValue();
				if(qtyOverride != null) withPricing.getOverridenQuantityProperty().set(qtyOverride);
				if(marginOverride != null) withPricing.getOverridenMarginProperty().set(marginOverride);
			}
			
			bom.add(withPricing);
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
		
		price.rebindPricing(this);
	}
	
	protected DoubleBinding getUnitPrice()
	{
		DoubleBinding binding = new SimpleDoubleProperty(0).add(0);
		
		DoubleBinding additionalMods = getAdditionalPriceModifiers();
		if(additionalMods != null) binding = binding.add(additionalMods);
		
		// This binding is used to separate values that are used to add value only when the build is non-catalog
		DoubleBinding nonCatalogValues = new SimpleDoubleProperty(0).add(0);
		for(BOMEntry bomEntry : bom)
		{
			DoubleBinding bomBinding = (DoubleBinding) Bindings.when(bomEntry.getIgnoreParentQuantityProperty()).then(new SimpleDoubleProperty(0).add(0)).otherwise(bomEntry.getUnitPriceProperty());
			if(bomEntry.getCustomEntryProperty().get()) { binding = binding.add(bomBinding); }
			else { nonCatalogValues = nonCatalogValues.add(bomBinding); }
		}
		for(RoutingEntry routingEntry : routings)
		{
			nonCatalogValues = nonCatalogValues.add(routingEntry.getUnitPriceProperty());
//			if(binding == null) binding = routingEntry.getUnitPriceProperty().add(0);
//			else binding = binding.add(routingEntry.getUnitPriceProperty());
		}
		for(Build childBuild : childBuilds)
		{
			binding = binding.add(childBuild.getPrice().getTotalPrice());
		}
		
		return (DoubleBinding) Bindings.when(catalogPrice.greaterThanOrEqualTo(0)).then(binding.add(catalogPrice)).otherwise(binding.add(nonCatalogValues));
	}
	
	protected DoubleBinding getTotalPrice()
	{
		DoubleBinding binding = getUnitPrice().multiply(this.quantity.getNumberProperty());
		
		for(BOMEntry bomEntry : bom)
		{
			DoubleBinding bomBinding = (DoubleBinding) Bindings.when(bomEntry.getIgnoreParentQuantityProperty().not()).then(new SimpleDoubleProperty(0).add(0)).otherwise(bomEntry.getTotalPriceProperty());
			if(binding == null) binding = bomBinding;
			else binding = binding.add(bomBinding);
		}
		
		return binding == null ? new ReadOnlyDoubleWrapper(0).add(0) : binding;
	}

	@Override
	public void addConflict(Conflict conflict) { conflicts.add(conflict); }

	@Override
	public void clearConflicts() { conflicts.clear(); }
}
