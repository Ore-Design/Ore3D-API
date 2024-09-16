package design.ore.Ore3DAPI.data.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Registry;
import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.Util.Log;
import design.ore.Ore3DAPI.Util.Mapper;
import design.ore.Ore3DAPI.data.ChildBuildMenuNode;
import design.ore.Ore3DAPI.data.Conflict;
import design.ore.Ore3DAPI.data.StoredValue;
import design.ore.Ore3DAPI.data.core.Transaction.BuildChangeType;
import design.ore.Ore3DAPI.data.interfaces.ValueStorageRecord;
import design.ore.Ore3DAPI.data.pricing.BOMEntry;
import design.ore.Ore3DAPI.data.pricing.MiscEntry;
import design.ore.Ore3DAPI.data.pricing.RoutingEntry;
import design.ore.Ore3DAPI.data.specs.IntegerSpec;
import design.ore.Ore3DAPI.data.specs.Spec;
import design.ore.Ore3DAPI.data.specs.StringSpec;
import design.ore.Ore3DAPI.data.wrappers.BuildList;
import design.ore.Ore3DAPI.data.wrappers.CatalogItem;
import design.ore.Ore3DAPI.jackson.BuildDataSerialization;
import design.ore.Ore3DAPI.jackson.ObservableListSerialization;
import design.ore.Ore3DAPI.jackson.ObservableSetSerialization;
import design.ore.Ore3DAPI.jackson.PropertySerialization;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.util.Pair;
import lombok.Getter;

@JsonInclude(Include.NON_NULL)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class Build extends ValueStorageRecord
{
//	private final ChangeListener<Boolean> childUpdateListener = new ChangeListener<Boolean>()
//	{
//		@Override
//		public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) { if(newVal) { buildIsDirty.setValue(true); } }
//	};

	private final ChangeListener<Boolean> childCatalogListener = (obs, oldVal, newVal) -> runCatalogDetection();
	
	@Getter protected int buildUUID = new Random().nextInt(111111, 1000000);
	public void regenerateBuildUUID() { buildUUID = new Random().nextInt(111111, 1000000); }
	
	@JsonMerge @Getter protected IntegerSpec quantity = new IntegerSpec(this, "Quantity", 1, false, "Overview", false, true);
	@JsonMerge @Getter protected StringSpec workOrder = new StringSpec(this, "Work Order", "", false, null, false);

	@JsonIgnore @JsonMerge @Getter protected final DoubleProperty catalogPrice = new SimpleDoubleProperty(-1);
	@JsonIgnore @Getter protected final BooleanBinding isCatalog = catalogPrice.greaterThanOrEqualTo(0);
	
	@JsonIgnore @Getter protected final ObjectProperty<Transaction> parentTransactionProperty = new SimpleObjectProperty<Transaction>();
	public boolean parentIsExpired() { return parentTransactionProperty.get() != null && parentTransactionProperty.get().isExpired(); }
	
	private final ReadOnlyBooleanWrapper buildIsDirty = new ReadOnlyBooleanWrapper(false);
	@JsonIgnore public final ReadOnlyBooleanProperty getIsDirtyProperty() { return buildIsDirty.getReadOnlyProperty(); }
	public final void setDirty()
	{
		if(parentTransactionProperty.isNotNull().get()) buildIsDirty.setValue(true);
		if(parentBuildProperty.isNotNull().get()) parentBuildProperty.get().setDirtyFromChild();
	}
	public final void setDirtyFromChild()
	{
		dirtyFromChild = true;
		buildIsDirty.setValue(true);
	}
	
	// This is used to prevent infinite looping of marking dirty from children
	@JsonIgnore private boolean dirtyFromChild = false;

	@JsonSerialize(using = BuildDataSerialization.BuildPriceSer.Serializer.class)
	@JsonDeserialize(using = BuildDataSerialization.BuildPriceSer.Deserializer.class)
	@JsonMerge @Getter protected final BuildPrice price;

	@JsonSerialize(using = PropertySerialization.StringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.StringSer.Deserializer.class)
	@Getter protected SimpleStringProperty titleProperty = new SimpleStringProperty("Build");
	
	// Description Stuff
	@JsonIgnore @JsonMerge final protected ReadOnlyStringWrapper unoverridenDescriptionProperty = new ReadOnlyStringWrapper("");
	
	@JsonSerialize(using = PropertySerialization.StringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.StringSer.Deserializer.class)
	@JsonMerge @Getter protected final SimpleStringProperty overridenDescriptionProperty = new SimpleStringProperty("");
	
	@JsonIgnore @Getter protected BooleanBinding descriptionIsOverridenBinding = overridenDescriptionProperty.isNotEqualTo(unoverridenDescriptionProperty);
	
	@JsonIgnore public String getDescription()
	{
		if(descriptionIsOverridenBinding.get()) return overridenDescriptionProperty.get();
		else return unoverridenDescriptionProperty.get();
	}
	public void resetDescription()
	{
		overridenDescriptionProperty.set(unoverridenDescriptionProperty.get());
	}
	
	@JsonIgnore protected final ReadOnlyObjectWrapper<Build> parentBuildProperty = new ReadOnlyObjectWrapper<>();
	public ReadOnlyObjectProperty<Build> getParentBuildProperty() { return parentBuildProperty.getReadOnlyProperty(); }
	
	@JsonIgnore @Getter protected BooleanBinding hasGeneratedWorkOrderBinding = workOrder.isNotEqualTo("")
			.or(Bindings.createBooleanBinding(() -> parentBuildProperty.getValue() != null ? parentBuildProperty.getValue().getHasGeneratedWorkOrderBinding().get() : false, parentBuildProperty));
	
	@JsonIgnore ReadOnlyBooleanWrapper buildHasConflicts = new ReadOnlyBooleanWrapper(false);
	public ReadOnlyBooleanProperty getBuildHasConflicts() { return buildHasConflicts.getReadOnlyProperty(); }
	
	@JsonIgnore public final BooleanBinding getCanGenerateWorkOrders()
	{
		BooleanBinding parentCanGenerate = Bindings.createBooleanBinding(() ->
		{
			if(parentBuildProperty.get() == null) return true;
			else
			{
				if(parentBuildProperty.get().getHasGeneratedWorkOrderBinding().get()) return false;
				else return true;
			}
		}, parentBuildProperty);
		
		BooleanBinding childrenCanGenerate = Bindings.createBooleanBinding(() -> { return childBuilds.stream().allMatch(cb -> cb.getCanGenerateWorkOrders().get()); }, childBuilds);
		
		return hasGeneratedWorkOrderBinding.and(buildHasConflicts).not().and(allowWorkOrders()).and(parentCanGenerate).and(childrenCanGenerate);
	}
	
	@Getter @JsonMerge
	protected final BuildList childBuilds = new BuildList();
	
	@JsonIgnore protected final ReadOnlyIntegerWrapper indexInParentWrapper = new ReadOnlyIntegerWrapper();
	@JsonIgnore public ReadOnlyIntegerProperty getIndexInParentProperty() { return indexInParentWrapper.getReadOnlyProperty(); }
	
	@JsonIgnore protected final ReadOnlyBooleanWrapper atEndOfParentWrapper = new ReadOnlyBooleanWrapper();
	@JsonIgnore public ReadOnlyBooleanProperty getAtEndOfParentProperty() { return atEndOfParentWrapper.getReadOnlyProperty(); }

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
	
	ChangeListener<Object> specChangeListener = (obs, oldVal, newVal) -> { if(oldVal == null || !oldVal.equals(newVal)) setDirty(); };
	
	@JsonIgnore private final ReadOnlyBooleanWrapper thisOrChildrenHaveMiscCharges = new ReadOnlyBooleanWrapper(false);
	@JsonIgnore public ReadOnlyBooleanProperty getThisOrChildrenHaveMiscCharges() { return thisOrChildrenHaveMiscCharges.getReadOnlyProperty(); }
	
	@JsonIgnore @Getter protected ObservableList<Spec<?>> specs;
	@JsonIgnore public abstract boolean allowUnitPriceOverride();
	
	@JsonIgnore @Getter private IntegerBinding childDepth = Bindings.createIntegerBinding(() ->
		parentBuildProperty.getValue() == null ? 0 : parentBuildProperty.get().getChildDepth().get() + 1, parentBuildProperty);
	
	public Build()
	{
		Mapper.getMapper().registerSubtypes(this.getClass());
		
		specs = FXCollections.observableArrayList();
		specs.addListener((ListChangeListener.Change<? extends Spec<?>> l) ->
		{
			while(l.next())
			{
				for(Spec<?> s : l.getAddedSubList()) { s.addListener(specChangeListener); }
				for(Spec<?> s : l.getRemoved()) { s.removeListener(specChangeListener); }
			}
		});
		
		this.price = new BuildPrice(this);
		
		unoverridenDescriptionProperty.addListener((obs, oldVal, newVal) ->
		{
			if(oldVal != null && newVal != null && overridenDescriptionProperty.isNotNull().get() && overridenDescriptionProperty.getValue().equals(oldVal))
			{
				overridenDescriptionProperty.set(newVal);
			}
		});
		
		childBuilds.addListener((ListChangeListener.Change<? extends Build> c) ->
		{
			while(c.next())
			{
				if(c.wasAdded())
				{
					for(Build cb : c.getAddedSubList())
					{
						if(cb.parentBuildProperty.get() != null) throw new IllegalArgumentException("The child build you are trying to add already has a parent!");
						
						cb.parentBuildProperty.setValue(this);
						cb.getParentTransactionProperty().bind(parentTransactionProperty);
						cb.getIsCatalog().addListener(childCatalogListener);
						
						checkIndex();
						
						if(parentTransactionProperty.get() != null) parentTransactionProperty.get().fireBuildListChangedEvent(BuildChangeType.ADDED, cb); // Event should be fire AFTER parent values are set
					}
				}
				else if(c.wasRemoved())
				{
					for(Build cb : c.getRemoved())
					{
						if(parentTransactionProperty.get() != null) parentTransactionProperty.get().fireBuildListChangedEvent(BuildChangeType.REMOVED, cb); // Event should be fire BEFORE parent values are removed
						
						cb.parentBuildProperty.setValue(null);
						cb.getParentTransactionProperty().unbind();
						cb.getIsCatalog().removeListener(childCatalogListener);
						
						checkIndex();
					}
				}
			}
			
			setDirty();
			rebindMiscListener();
		});
		
		misc.addListener((ListChangeListener.Change<? extends MiscEntry> c) ->
		{
			while(c.next())
			{
				for(MiscEntry me : c.getAddedSubList()) { me.rebind(quantity); }
			}
		});
		
		buildIsDirty.addListener((obs, oldVal, newVal) ->
		{
			// Refresh must be called AFTER dirty listeners to avoid mismatch data
			if(newVal) refresh();
			
			if(parentBuildProperty.isNotNull().get()) parentBuildProperty.get().setDirtyFromChild();
			
			buildIsDirty.setValue(false);
		});
		
		quantity.setCalculateOnDirty(() ->
		{
			if(quantity.getValue() <= 0) return 1;
			else return null;
		});
		
		specs.addAll(quantity, workOrder);
		
		parentTransactionProperty.addListener((obs, oldVal, newVal) ->
		{
			if(oldVal != null)
			{
				buildHasConflicts.unbind();
				oldVal.removeOnBuildListChangedListener(indexCheckConsumer);
				oldVal.getBuilds().removeListener(indexCheckListener);
			}
			if(newVal != null)
			{
				buildHasConflicts.bind(newVal.buildHasConflicts(this));
				newVal.addOnBuildListChangedListener(indexCheckConsumer);
				newVal.getBuilds().addListener(indexCheckListener);
			}
			checkIndex();
		});
		
		parentBuildProperty.addListener((obs, oldVal, newVal) -> { if(newVal != null) checkIndex(); });
		
		rebindMiscListener();
	}
	
	ListChangeListener<Build> indexCheckListener = (ListChangeListener.Change<? extends Build> change) -> checkIndex();
	BiConsumer<BuildChangeType, Build> indexCheckConsumer = (ct, b) -> checkIndex();
	
	public final void checkIndex()
	{
		if(parentBuildProperty.get() != null)
		{
			indexInParentWrapper.set(parentBuildProperty.get().getChildBuilds().indexOf(this));
			atEndOfParentWrapper.set(indexInParentWrapper.get() == getParentBuildProperty().get().getChildBuilds().size() - 1);
		}
		else if(parentTransactionProperty.get() != null)
		{
			indexInParentWrapper.set(parentTransactionProperty.get().getBuilds().indexOf(this));
			atEndOfParentWrapper.set(indexInParentWrapper.get() == getParentTransactionProperty().get().getBuilds().size() - 1);
		}
		else 
		{
			indexInParentWrapper.set(-1);
			atEndOfParentWrapper.set(true);
		}
	}
	
	protected void rebindMiscListener()
	{
		BooleanBinding childrenMiscBinding = Bindings.createBooleanBinding(() -> misc.size() > 0, misc);
		for(Build cb : childBuilds)
		{ childrenMiscBinding = childrenMiscBinding.or(cb.getThisOrChildrenHaveMiscCharges()); }
		
		thisOrChildrenHaveMiscCharges.bind(childrenMiscBinding);
	}
	
	@JsonIgnore @Getter protected final ObservableList<ChildBuildMenuNode> allowedChildBuilds = FXCollections.observableArrayList();

	public abstract List<BOMEntry> calculateStandardBOMs();
	public abstract List<RoutingEntry> calculateRoutings();
	public abstract StringExpression calculateDefaultDescription();
	protected abstract DoubleBinding getAdditionalPriceModifiers();
	protected abstract void detectConflicts();
	public abstract BooleanBinding allowWorkOrders();
	
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
		duplicate.workOrder.setValue("");
		Registry.handleBuildDuplicate(duplicate);
		
		return duplicate;
	}
	
	@JsonIgnore public Map<Integer, Build> getAllChildBuildsByUID()
	{
		Map<Integer, Build> allBuilds = new HashMap<>();
		for(Build cb : childBuilds)
		{
			allBuilds.put(cb.getBuildUUID(), cb);
			allBuilds.putAll(cb.getAllChildBuildsByUID());
		}
		return allBuilds;
	}
	
	public boolean matches(Build toMatch)
	{
		if(!this.getClass().equals(toMatch.getClass())) return false;
		
		for(Spec<?> s : this.specs)
		{
			if(s.countsAsMatch())
			{
				Optional<Spec<?>> optionalMatch = toMatch.specs.stream().filter(sp -> sp.getId().equals(s.getId())).findFirst();
				if(optionalMatch.isEmpty()) return false;
				Spec<?> matching = optionalMatch.get();
				if((s.getValue() == null && matching.getValue() != null) || (s.getValue() != null && matching.getValue() == null) ||
					(s.getValue() != null && matching.getValue() != null && !matching.getValue().equals(s.getValue()))) return false;
			}
		}
		
		for(Spec<?> s : toMatch.specs)
		{
			if(s.countsAsMatch())
			{
				Optional<Spec<?>> optionalMatch = this.specs.stream().filter(sp -> sp.getId().equals(s.getId())).findFirst();
				if(optionalMatch.isEmpty()) return false;
				Spec<?> matching = optionalMatch.get();
				if((s.getValue() == null && matching.getValue() != null) || (s.getValue() != null && matching.getValue() == null) ||
					(s.getValue() != null && matching.getValue() != null && !matching.getValue().equals(s.getValue()))) return false;
			}
		}
		
		return true;
	}
	
	protected void refresh()
	{
		if(!dirtyFromChild)
		{
			for(Build cb : childBuilds) { cb.refresh(); }
		}
		else dirtyFromChild = false;
		
		Transaction parentTran = parentTransactionProperty.get();
		if(parentTran != null && !parentTran.isExpired())
		{	
			parentTran.removeConflictsForBuild(getBuildUUID());
			
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
			binding = binding.add(Bindings.when(miscEntry.getIgnoreParentQuantityProperty()).then(new SimpleDoubleProperty(0).add(0)).otherwise(miscEntry.getUnitPriceProperty()));
		}
		for(Build childBuild : childBuilds)
		{
			binding = binding.add(childBuild.getTotalPrice());
		}
		
		return (DoubleBinding) Bindings.when(isCatalog).then(binding.add(catalogPrice)).otherwise(binding.add(nonCatalogValues));
	}
	
	public NumberBinding getTotalPrice()
	{
		if(price.totalPriceOverriddenProperty.get()) return price.totalPrice;
		
		NumberBinding binding = price.totalPrice;
		
		for(BOMEntry bomEntry : bom)
		{
			DoubleBinding bomBinding = (DoubleBinding) Bindings.when(bomEntry.getIgnoreParentQuantityProperty().not()).then(new SimpleDoubleProperty(0).add(0)).otherwise(bomEntry.getTotalPriceProperty());
			if(binding == null) binding = bomBinding;
			else binding = binding.add(bomBinding);
		}
		
		for(MiscEntry miscEntry : misc)
		{
			DoubleBinding bomBinding = (DoubleBinding) Bindings.when(miscEntry.getIgnoreParentQuantityProperty().not()).then(new SimpleDoubleProperty(0).add(0)).otherwise(miscEntry.getTotalPriceProperty());
			if(binding == null) binding = bomBinding;
			else binding = binding.add(bomBinding);
		}
		
		return binding == null ? new ReadOnlyDoubleWrapper(0).add(0) : binding;
	}

	public void addConflict(Conflict conflict)
	{
		if(parentTransactionProperty.get() != null) parentTransactionProperty.get().addConflict(conflict);
	}
	
	private final void runCatalogDetection()
	{
		for(Build cb : getChildBuilds()) { cb.runCatalogDetection(); }
		
		CatalogItem catalog = null;
		for(CatalogItem ci : Registry.getRegisteredCatalogItems())
		{
			if(this.matches(ci.getBuild()))
			{
				catalog = ci;
				break;
			}
		}
		
		if(catalog != null)
		{
			boolean parentIsCatalog = parentBuildProperty.isNotNull().get() && parentBuildProperty.get().isCatalog.get();
			boolean childrenHaveNonCatalog = getChildBuilds().stream().anyMatch(cb -> !cb.isCatalog.get());
			
			if((parentIsCatalog || parentBuildProperty.isNull().get() || !Registry.isChildrenOnlyCatalogIfParentIsCatalog()) &&
				(!childrenHaveNonCatalog || !Registry.isCustomChildrenPreventCatalogParents() || childBuilds.size() == 0))
			{
				catalogPrice.set(catalog.getPrice());
				return;
			}
		}

		catalogPrice.set(-1);
	}
	
	public void forceResetCatalog()
	{
		catalogPrice.set(catalogPrice.get() > 0 ? -1 : 1);
		runCatalogDetection();
		unoverridenDescriptionProperty.bind(calculateDefaultDescription());
	}
	
	@Override
	public String toString()
	{
		return titleProperty.get() + " - " + buildUUID + " (Index In Parent: " + indexInParentWrapper.get() + ")";
	}
}
