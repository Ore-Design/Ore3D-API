package design.ore.Ore3DAPI.DataTypes.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Registry;
import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.DataTypes.Conflict;
import design.ore.Ore3DAPI.DataTypes.StoredValue;
import design.ore.Ore3DAPI.DataTypes.CRM.Transaction;
import design.ore.Ore3DAPI.DataTypes.Interfaces.Conflictable;
import design.ore.Ore3DAPI.DataTypes.Interfaces.ValueStorageRecord;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.MiscEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.Spec;
import design.ore.Ore3DAPI.DataTypes.Specs.StringSpec;
import design.ore.Ore3DAPI.DataTypes.Wrappers.CatalogItem;
import design.ore.Ore3DAPI.Jackson.BuildDataSerialization;
import design.ore.Ore3DAPI.Jackson.ObservableListSerialization;
import design.ore.Ore3DAPI.Jackson.ObservableSetSerialization;
import design.ore.Ore3DAPI.Jackson.PropertySerialization;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
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
	
	@JsonMerge @Getter protected PositiveIntSpec quantity = new PositiveIntSpec(this, "Quantity", 1, false, "Overview", false);
	@JsonMerge @Getter protected StringSpec workOrder = new StringSpec(this, "Work Order", "", false, null, false);

	@JsonSerialize(using = PropertySerialization.DoubleSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.DoubleSer.Deserializer.class)
	@JsonMerge @Getter protected DoubleProperty catalogPrice = new SimpleDoubleProperty(-1);
	@JsonIgnore @Getter protected BooleanBinding isCatalog = catalogPrice.greaterThanOrEqualTo(0);
	
	@JsonIgnore @Getter protected final ObjectProperty<Transaction> parentTransactionProperty = new SimpleObjectProperty<Transaction>();
	public boolean parentIsExpired() { return parentTransactionProperty.get() != null && parentTransactionProperty.get().isExpired(); }
	
	private final SimpleBooleanProperty buildIsDirty = new SimpleBooleanProperty(false);
	public void setDirty() { if(parentTransactionProperty.isNotNull().get()) buildIsDirty.setValue(true); }
	
	private Map<String, ChangeListener<Boolean>> registeredDirtyUpdates = new HashMap<>();
	public void registerDirtyListenerEvent(String listenerID, ChangeListener<Boolean> listener)
	{
		if(registeredDirtyUpdates.containsKey(listenerID)) Util.Log.getLogger().warn("Attempted to add listener " + listenerID + " to build, but its already registered!");
		else registeredDirtyUpdates.put(listenerID, listener);
	}
	public void unregisterDirtyListenerEvent(String listenerID) { registeredDirtyUpdates.remove(listenerID); }

	@JsonSerialize(using = BuildDataSerialization.BuildPriceSer.Serializer.class)
	@JsonDeserialize(using = BuildDataSerialization.BuildPriceSer.Deserializer.class)
	@JsonMerge @Getter protected BuildPrice price;

	@JsonSerialize(using = PropertySerialization.StringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.StringSer.Deserializer.class)
	@Getter protected SimpleStringProperty titleProperty = new SimpleStringProperty("Build");
	
	// Description Stuff
	@JsonSerialize(using = PropertySerialization.ReadOnlyStringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.ReadOnlyStringSer.Deserializer.class)
	@JsonMerge final protected ReadOnlyStringWrapper unoverridenDescriptionProperty = new ReadOnlyStringWrapper("");
	
	@JsonSerialize(using = PropertySerialization.StringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.StringSer.Deserializer.class)
	@JsonMerge @Getter protected SimpleStringProperty overridenDescriptionProperty = new SimpleStringProperty("");
	
	@JsonIgnore @Getter protected BooleanBinding descriptionIsOverridenBinding = overridenDescriptionProperty.isNotEqualTo("").and(overridenDescriptionProperty.isNotEqualTo(unoverridenDescriptionProperty));
	@JsonIgnore @Getter protected StringBinding descriptionBinding = Bindings.when(descriptionIsOverridenBinding).then(overridenDescriptionProperty).otherwise(unoverridenDescriptionProperty);
	
	@JsonIgnore protected ReadOnlyObjectWrapper<Build> parentBuildProperty = new ReadOnlyObjectWrapper<>();
	public ReadOnlyObjectProperty<Build> getParentBuildProperty() { return parentBuildProperty.getReadOnlyProperty(); }
	
	@JsonIgnore @Getter protected BooleanBinding hasGeneratedWorkOrderBinding = workOrder.getProperty().isNotEqualTo("")
			.or(Bindings.createBooleanBinding(() -> parentBuildProperty.getValue() != null ? parentBuildProperty.getValue().getHasGeneratedWorkOrderBinding().get() : false, parentBuildProperty));

	// We have to serialize child builds using a custom setter, otherwise linking children to parent fails.
	// 1/11/24 SIKE!, we changed to using @JsonMerge annotation to do the same thing.
	@JsonDeserialize(using = ObservableListSerialization.BuildList.Deserializer.class)
	@JsonSerialize(using = ObservableListSerialization.BuildList.Serializer.class)
	@Getter @JsonMerge
	protected final ObservableList<Build> childBuilds = FXCollections.observableArrayList();

	@JsonSerialize(using = ObservableSetSerialization.IntSet.Serializer.class)
	@JsonDeserialize(using = ObservableSetSerialization.IntSet.Deserializer.class)
	@Getter @JsonMerge
	protected ObservableSet<Integer> tags = FXCollections.observableSet();

	@JsonSerialize(using = ObservableSetSerialization.StringSet.Serializer.class)
	@JsonDeserialize(using = ObservableSetSerialization.StringSet.Deserializer.class)
	@JsonMerge protected final ObservableSet<String> queryableValues = FXCollections.observableSet();
	@JsonIgnore @Getter private final ObservableSet<String> readOnlyQueryableValues = FXCollections.unmodifiableObservableSet(queryableValues);
	public boolean addQueryableValue(String val) { return queryableValues.add(val); }
	public boolean removeQueryableValue(String val) { return queryableValues.remove(val); }
	
	@JsonDeserialize(using = ObservableListSerialization.BOMEntryList.Deserializer.class)
	@JsonSerialize(using = ObservableListSerialization.BOMEntryList.Serializer.class)
	@Getter @JsonMerge
	protected ObservableList<BOMEntry> bom = FXCollections.observableArrayList();
	
	@JsonDeserialize(using = ObservableListSerialization.RoutingEntryList.Deserializer.class)
	@JsonSerialize(using = ObservableListSerialization.RoutingEntryList.Serializer.class)
	@Getter @JsonMerge
	protected ObservableList<RoutingEntry> routings = FXCollections.observableArrayList();

	@JsonDeserialize(using = ObservableListSerialization.MiscEntryList.Deserializer.class)
	@JsonSerialize(using = ObservableListSerialization.MiscEntryList.Serializer.class)
	@Getter @JsonMerge
	protected ObservableList<MiscEntry> misc = FXCollections.observableArrayList();

	@JsonSerialize(using = ObservableListSerialization.ConflictList.Serializer.class)
	@JsonDeserialize(using = ObservableListSerialization.ConflictList.Deserializer.class)
	@Getter @JsonMerge
	protected ObservableList<Conflict> conflicts = FXCollections.observableArrayList();
	
	@JsonIgnore @Getter protected ObservableList<Spec<?>> specs;
	@JsonIgnore public abstract boolean allowUnitPriceOverride();
	
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
						cb.parentTransactionProperty.bind(parentTransactionProperty);
						cb.registerDirtyListenerEvent("ChildUpdateListener", childUpdateListener);
						this.setDirty();
					}
				}
				else if(l.wasRemoved())
				{
					for(Build cb : l.getRemoved())
					{
						cb.parentBuildProperty.setValue(null);
						cb.parentTransactionProperty.unbind();
						cb.unregisterDirtyListenerEvent("ChildUpdateListener");
						setDirty();
					}
				}
			}
		});
		
		buildIsDirty.addListener((obs, oldVal, newVal) ->
		{
			for(Build cb : childBuilds)
			{
				// Refresh must be called AFTER dirty listeners to avoid mismatch data
				for(ChangeListener<Boolean> listener : cb.registeredDirtyUpdates.values()) listener.changed(obs, oldVal, newVal);
				if(newVal) cb.refresh();
			}
			
			// Refresh must be called AFTER dirty listeners to avoid mismatch data
			for(ChangeListener<Boolean> listener : registeredDirtyUpdates.values()) listener.changed(obs, oldVal, newVal);
			if(newVal) refresh();
			
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
						s.addListener((obsv, oldVal, newVal) -> { if((oldVal == null || !oldVal.equals(newVal)) && !s.getReadOnlyProperty().get()) setDirty(); });
					}
				}
				if(l.wasRemoved()) throw new IllegalArgumentException("Removing specs from specs list is not supported!");
			}
		});
		
		specs.addAll(quantity, workOrder);
		
//		registerDirtyListenerEvent("CalculateOnDirty", (obs, oldVal, newVal) ->
//		{
//			if(!newVal) { for(Spec<?> sp : specs) { sp.setPropertyToCallable(); } }
//		});
	}
	
	@JsonIgnore @Getter protected final ObservableMap<String, Build> allowedChildBuilds = FXCollections.observableHashMap();

	public abstract List<BOMEntry> calculateStandardBOMs();
	public abstract List<RoutingEntry> calculateRoutings();
	public abstract StringExpression calculateDefaultDescription();
	protected abstract DoubleBinding getAdditionalPriceModifiers();
	protected abstract void detectConflicts();
	
	public final Build duplicate()
	{
		Build duplicate = null;
		try
		{
			String original = Util.Mapper.getMapper().writeValueAsString(this);
			Log.getLogger().debug("Duplicated build JSON: " + original);
			duplicate = Util.Mapper.getMapper().readValue(original, Build.class);
		}
		catch (JsonProcessingException e) { Util.Log.getLogger().error("An error has occured while duplicating build!\n" + e.getMessage() + "\n" + Util.stackTraceArrayToString(e)); }
		
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
		
		if(!this.getClass().equals(toMatch.getClass())) return false;
		
		for(Spec<?> s : this.specs)
		{
			if(s.countsAsMatch())
			{
				Optional<Spec<?>> optionalMatch = toMatch.specs.stream().filter(sp -> sp.getId() == s.getId()).findFirst();
				if(optionalMatch.isEmpty()) return false;
				Spec<?> matching = optionalMatch.get();
				if(s.getValue() == null || matching.getValue() == null || !matching.getValue().equals(s.getValue())) return false;
			}
		}
		
		return true;
	}
	
	protected void refresh()
	{	
		Transaction parentTran = parentTransactionProperty.get();
		if(parentTran != null && !parentTran.isExpired())
		{
			conflicts.clear();
			
			for(Spec<?> sp : specs) { sp.setPropertyToCallable(); }
			
			runCatalogDetection();
	
			Map<String, Pair<Double, Integer>> overriddenStandardBOMS = new HashMap<>();
			
			unoverridenDescriptionProperty.bind(calculateDefaultDescription());
			
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
	
			// We only clear non-custom Routings, hence the usage of routingToRemove
			List<RoutingEntry> routingToRemove = new ArrayList<>();
			Map<String, Double> overriddenRoutings = new HashMap<>();
			for(RoutingEntry e : routings)
			{
				if(!e.getCustomEntryProperty().get())
				{
					Double quantityOverride = null;
					if(e.getQuantityOverriddenProperty().get()) quantityOverride = e.getOverridenQuantityProperty().get();
					if(quantityOverride != null) overriddenRoutings.put(e.getId(), quantityOverride);
	
					routingToRemove.add(e);
				}
			}
			
			bom.removeAll(bomToRemove);
			routings.removeAll(routingToRemove);
			
			for(BOMEntry e : calculateStandardBOMs())
			{
				if(e == null) continue;
				
				BOMEntry newBOM = e;
				if(this.parentBuildProperty.get() != null)
				{
					if(parentTran != null) newBOM = Util.duplicateBOMWithPricing(parentTran, this, e, e);
					else Util.Log.getLogger().debug("No transaction parent is registered for the top-level parent of build " + this.getTitleProperty().get() + ", so pricing for generated BOMs cant be matched to transaction!");
				}
				else if(parentTransactionProperty.get() != null) newBOM = Util.duplicateBOMWithPricing(parentTransactionProperty.get(), this, e, e);
				else Util.Log.getLogger().debug("No transaction parent is registered for the build " + this.getTitleProperty().get() + ", so pricing for generated BOMs cant be matched to transaction!");
					
				if(overriddenStandardBOMS.containsKey(e.getId()))
				{
					Pair<Double, Integer> overrides = overriddenStandardBOMS.get(e.getId());
					Double qtyOverride = overrides.getKey();
					Integer marginOverride = overrides.getValue();
					if(qtyOverride != null) newBOM.getOverridenQuantityProperty().set(qtyOverride);
					if(marginOverride != null) newBOM.getOverridenMarginProperty().set(marginOverride);
				}
				
				for(Entry<String, StoredValue> entry : Registry.getRegisteredBOMEntryStoredValues().entrySet()) { newBOM.putStoredValue(entry.getKey(), entry.getValue().duplicate()); }
				bom.add(newBOM);
			}
			
			for(RoutingEntry r : calculateRoutings())
			{
				if(overriddenRoutings.containsKey(r.getId()))
				{
					Double overrides = overriddenRoutings.get(r.getId());
					if(overrides != null) r.getOverridenQuantityProperty().set(overrides);
				}
	
				for(Entry<String, StoredValue> entry : Registry.getRegisteredRoutingEntryStoredValues().entrySet()) { r.putStoredValue(entry.getKey(), entry.getValue().duplicate()); }
				routings.add(r);
			}
			
			detectConflicts();
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
			if(routingEntry.getCustomEntryProperty().get()) binding = binding.add(routingEntry.getUnitPriceProperty());
			else { nonCatalogValues = nonCatalogValues.add(routingEntry.getUnitPriceProperty()); }
		}
		for(MiscEntry miscEntry : misc)
		{
			binding = binding.add(miscEntry.getUnitPriceProperty());
		}
		for(Build childBuild : childBuilds)
		{
			binding = binding.add(childBuild.getPrice().getTotalPrice());
		}
		
		return (DoubleBinding) Bindings.when(isCatalog).then(binding.add(catalogPrice)).otherwise(binding.add(nonCatalogValues));
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
	
	private void runCatalogDetection()
	{
//		Log.getLogger().debug("Running catalog detection for " + titleProperty.get() +  "...");
		for(Build cb : getChildBuilds()) { cb.runCatalogDetection(); }
		
		boolean foundCatalog = false;
		for(CatalogItem ci : Registry.getRegisteredCatalogItems())
		{
			if(ci.getBuild().matches(this))
			{
				catalogPrice.set(ci.getPrice());
				foundCatalog = true;
				break;
			}
		}
		
		if(!foundCatalog) catalogPrice.set(-1);
	}
}
