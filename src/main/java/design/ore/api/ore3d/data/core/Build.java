package design.ore.api.ore3d.data.core;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import design.ore.api.ore3d.Registry;
import design.ore.api.ore3d.Util;
import design.ore.api.ore3d.Util.Log;
import design.ore.api.ore3d.Util.Mapper;
import design.ore.api.ore3d.data.ChildBuildMenuNode;
import design.ore.api.ore3d.data.Conflict;
import design.ore.api.ore3d.data.StoredValue;
import design.ore.api.ore3d.data.core.Transaction.BuildChangeType;
import design.ore.api.ore3d.data.interfaces.ValueStorageRecord;
import design.ore.api.ore3d.data.pricing.*;
import design.ore.api.ore3d.data.specs.IntegerSpec;
import design.ore.api.ore3d.data.specs.Spec;
import design.ore.api.ore3d.data.specs.StringSpec;
import design.ore.api.ore3d.data.wrappers.BuildList;
import design.ore.api.ore3d.data.wrappers.CatalogItem;
import design.ore.api.ore3d.jackson.BuildDataSerialization;
import design.ore.api.ore3d.jackson.ObservableListSerialization;
import design.ore.api.ore3d.jackson.ObservableSetSerialization;
import design.ore.api.ore3d.jackson.PropertySerialization;
import javafx.beans.Observable;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.util.Pair;
import lombok.Getter;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

@JsonInclude(Include.NON_NULL)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class Build extends ValueStorageRecord
{
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

	// These are stable, long-lived DoubleBindings, built exactly once per Build instance and never rebuilt.
	// Real dependencies are registered ONCE (via bindDependencies(), called from the constructor below) using
	// the standard protected bind(Observable...) mechanism, so they invalidate automatically - and lazily/pull-
	// style, i.e. computeValue() only runs when something actually reads the value - whenever isCatalog,
	// catalogPrice, or the bom/routings/misc lists (or a tracked property on one of their current elements, via
	// their extractors) change, or a child is added/removed. Each child's own totalPrice is bound/unbound
	// individually as children come and go (see the childBuilds listener in the constructor) rather than via an
	// extractor on childBuilds itself - childBuilds already has a structural-change listener with non-idempotent
	// side effects, and an extractor there would turn every child price recalculation into a structural-change
	// notification and re-enter it. refresh() additionally calls markDirty() for things these dependencies can't
	// cover (e.g. a subclass's imperative, non-reactive price modifier lookups).
	// Rebuilding a fresh Binding tree every refresh() (the original design) leaked WeakReference-wrapped listener
	// registrations onto isCatalog/catalogPrice on every recalculation. Recomputing eagerly on every refresh()
	// (an earlier attempt at this fix) was leak-free but forced a full eager price recompute of the entire build
	// tree on every spec edit. Binding dependencies once, like this, avoids both problems.
	private class RecalculatedDoubleBinding extends DoubleBinding
	{
		private final java.util.function.DoubleSupplier supplier;
		private RecalculatedDoubleBinding(java.util.function.DoubleSupplier supplier) { this.supplier = supplier; }
		@Override protected double computeValue() { return supplier.getAsDouble(); }
		private void bindDependencies(Observable... dependencies) { bind(dependencies); }
		private void unbindDependencies(Observable... dependencies) { unbind(dependencies); }
		private void markDirty() { invalidate(); }
	}

	@JsonIgnore private final RecalculatedDoubleBinding unitPriceBinding = new RecalculatedDoubleBinding(this::calculateUnitPrice);
	@JsonIgnore private final RecalculatedDoubleBinding totalPriceBinding = new RecalculatedDoubleBinding(this::calculateTotalPrice);

	@JsonSerialize(using = BuildDataSerialization.BuildPriceSer.Serializer.class)
	@JsonDeserialize(using = BuildDataSerialization.BuildPriceSer.Deserializer.class)
	@JsonMerge @Getter protected final BuildPrice price;

	@JsonSerialize(using = PropertySerialization.StringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.StringSer.Deserializer.class)
	@Getter protected SimpleStringProperty titleProperty = new SimpleStringProperty("Build");
	
	@JsonIgnore protected final ReadOnlyBooleanWrapper titleEditableProperty = new ReadOnlyBooleanWrapper(true);
	@JsonIgnore public ReadOnlyBooleanProperty getTitleEditableProperty() { return titleEditableProperty.getReadOnlyProperty(); }
	@JsonIgnore public boolean isTitleEditable() { return titleEditableProperty.get(); }
	
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
	
	// Extractors let the list itself fire an update event when a tracked property on one of its current elements
	// changes (a margin/quantity override, say) - not just on add/remove - so the price bindings below (which
	// depend on these lists) invalidate correctly without needing every such edit funneled through refresh().
	@JsonDeserialize(using = ObservableListSerialization.BOMEntryList.Deserializer.class)
	@JsonSerialize(using = ObservableListSerialization.BOMEntryList.Serializer.class)
	@Getter @JsonMerge
	protected ObservableList<BOMEntry> bom = FXCollections.observableArrayList(e ->
		new Observable[] { e.getCustomEntryProperty(), e.getIgnoreParentQuantityProperty(), e.getUnitPriceProperty(), e.getTotalPriceProperty() });

	@JsonDeserialize(using = ObservableListSerialization.RoutingEntryList.Deserializer.class)
	@JsonSerialize(using = ObservableListSerialization.RoutingEntryList.Serializer.class)
	@Getter @JsonMerge
	protected ObservableList<RoutingEntry> routings = FXCollections.observableArrayList(e ->
		new Observable[] { e.getCustomEntryProperty(), e.getUnitPriceProperty() });

	@JsonDeserialize(using = ObservableListSerialization.MiscEntryList.Deserializer.class)
	@JsonSerialize(using = ObservableListSerialization.MiscEntryList.Serializer.class)
	@Getter @JsonMerge
	protected ObservableList<MiscEntry> misc = FXCollections.observableArrayList(e ->
		new Observable[] { e.getIgnoreParentQuantityProperty(), e.getUnitPriceProperty(), e.getTotalPriceProperty() });
	
	ChangeListener<Object> specChangeListener = (obs, oldVal, newVal) -> { if(oldVal == null || !oldVal.equals(newVal)) setDirty(); };
	
	@JsonIgnore private final ReadOnlyBooleanWrapper thisOrChildrenHaveMiscCharges = new ReadOnlyBooleanWrapper(false);
	@JsonIgnore public ReadOnlyBooleanProperty getThisOrChildrenHaveMiscCharges() { return thisOrChildrenHaveMiscCharges.getReadOnlyProperty(); }
	
	@JsonIgnore @Getter protected ObservableList<Spec<?>> specs;
	@JsonIgnore public abstract boolean allowUnitPriceOverride();
	
	@JsonIgnore @Getter private IntegerBinding childDepth = Bindings.createIntegerBinding(() ->
		parentBuildProperty.getValue() == null ? 0 : parentBuildProperty.get().getChildDepth().get() + 1, parentBuildProperty);

	public void addBom(BOMEntry bomEntry)
	{
		List<BOMPricing> bomPricing = parentTransactionProperty.isNotNull().get() ? parentTransactionProperty.get().pricing.getBom() : List.of();
		Optional<BOMPricing> matchingPricingOpt = bomPricing.stream().filter(rp -> rp.getInternalID().equals(bomEntry.getId())).findFirst();
		matchingPricingOpt.ifPresent(pricing -> bomEntry.setUnoverriddenMargin(pricing.getMargin()));
		bom.add(bomEntry);
	}

	public void addRouting(RoutingEntry routingEntry)
	{
		List<RoutingPricing> bomPricing = parentTransactionProperty.isNotNull().get() ? parentTransactionProperty.get().pricing.getRoutings() : List.of();
		Optional<RoutingPricing> matchingPricingOpt = bomPricing.stream().filter(rp -> rp.getId().equals(routingEntry.getId())).findFirst();
		matchingPricingOpt.ifPresent(pricing -> routingEntry.setMargin(pricing.getMargin()));
		routings.add(routingEntry);
	}
	
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

		unitPriceBinding.bindDependencies(bom, routings, misc, childBuilds, isCatalog, catalogPrice);
		totalPriceBinding.bindDependencies(price.totalPriceOverriddenProperty, price.totalPrice, bom, misc);

		// unitPriceBinding sums each child's totalPrice, so it needs to be bound to each child's totalPrice
		// individually (bound/unbound as children are added/removed) - see the comment on RecalculatedDoubleBinding
		// above for why this isn't done via an extractor on childBuilds instead.
		for(Build cb : childBuilds) { unitPriceBinding.bindDependencies(cb.getTotalPrice()); }
		childBuilds.addListener((ListChangeListener.Change<? extends Build> c) ->
		{
			while(c.next())
			{
				for(Build cb : c.getAddedSubList()) { unitPriceBinding.bindDependencies(cb.getTotalPrice()); }
				for(Build cb : c.getRemoved()) { unitPriceBinding.unbindDependencies(cb.getTotalPrice()); }
			}
		});


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
						
						if(parentTransactionProperty.get() != null) parentTransactionProperty.get().fireBuildListChangedEvent(BuildChangeType.ADDED, cb); // Event should fire AFTER parent values are set
					}
				}
				else if(c.wasRemoved())
				{
					for(Build cb : c.getRemoved())
					{
						if(parentTransactionProperty.get() != null) parentTransactionProperty.get().fireBuildListChangedEvent(BuildChangeType.REMOVED, cb); // Event should fire BEFORE parent values are removed
						
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
				for(MiscEntry me : c.getAddedSubList()) { me.setParentBuild(this); }
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
	public abstract String calculateDefaultDescription();
	protected abstract double getAdditionalPriceModifiers();
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
		catch (JsonProcessingException e) { Util.Log.getLogger().error("An error has occurred while duplicating build!", e); }
		
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
	
	public void refresh()
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
			
			unoverridenDescriptionProperty.set(calculateDefaultDescription());
			
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

			List<BOMPricing> bomPricing = parentTransactionProperty.isNotNull().get() ? parentTransactionProperty.get().pricing.getBom() : List.of();
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

				Optional<BOMPricing> matchingPricingOpt = bomPricing.stream().filter(rp -> rp.getInternalID().equals(e.getId())).findFirst();

				matchingPricingOpt.ifPresent(pricing -> e.setUnoverriddenMargin(pricing.getMargin()));
				
				for(Entry<String, StoredValue> entry : Registry.getRegisteredBOMEntryStoredValues().entrySet()) { newBOM.putStoredValue(entry.getKey(), entry.getValue().duplicate()); }
				bom.add(newBOM);
			}

			List<RoutingPricing> routingPricing = parentTransactionProperty.isNotNull().get() ? parentTransactionProperty.get().pricing.getRoutings() : List.of();
			
			for(RoutingEntry r : calculateRoutings())
			{
				Optional<RoutingPricing> matchingPricingOpt = routingPricing.stream().filter(rp -> rp.getId().equals(r.getId())).findFirst();

                matchingPricingOpt.ifPresent(pricing -> r.setMargin(pricing.getMargin()));

				if(overriddenRoutings.containsKey(r.getId()))
				{
					Double overrides = overriddenRoutings.get(r.getId());
					if(overrides != null) r.getOverridenQuantityProperty().set(overrides);
				}
	
				for(Entry<String, StoredValue> entry : Registry.getRegisteredRoutingEntryStoredValues().entrySet()) { r.putStoredValue(entry.getKey(), entry.getValue().duplicate()); }

				addRouting(r);
			}

			detectConflicts();
		}

		price.rebindPricing(this);
		unitPriceBinding.markDirty();
		totalPriceBinding.markDirty();
	}

	// Extension point for subclasses (e.g. OreBuild) to layer additional pricing logic on top of the base
	// calculation. Called once per refresh() - do not wrap this in a fresh JavaFX Binding tree, just do plain math.
	protected double calculateUnitPrice()
	{
		double bindingTotal = getAdditionalPriceModifiers();

		// This accumulator is used to separate values that are used to add value only when the build is non-catalog
		double nonCatalogValues = 0;
		for(BOMEntry bomEntry : bom)
		{
			double bomValue = bomEntry.getIgnoreParentQuantityProperty().get() ? 0 : bomEntry.getUnitPriceProperty().get();
			if(bomEntry.getCustomEntryProperty().get()) bindingTotal += bomValue;
			else nonCatalogValues += bomValue;
		}
		for(RoutingEntry routingEntry : routings)
		{
			double routingValue = routingEntry.getUnitPriceProperty().get();
			if(routingEntry.getCustomEntryProperty().get()) bindingTotal += routingValue;
			else nonCatalogValues += routingValue;
		}
		for(MiscEntry miscEntry : misc)
		{
			bindingTotal += miscEntry.getIgnoreParentQuantityProperty().get() ? 0 : miscEntry.getUnitPriceProperty().get();
		}
		for(Build childBuild : childBuilds)
		{
			bindingTotal += childBuild.getTotalPrice().get();
		}

		return isCatalog.get() ? bindingTotal + catalogPrice.get() : bindingTotal + nonCatalogValues;
	}

	// Stable, long-lived, lazily-evaluated binding - safe to bind UI to. refresh() marks it dirty; the actual sum
	// in calculateUnitPrice() only runs when something reads the value.
	protected final DoubleBinding getUnitPrice() { return unitPriceBinding; }

	// This is what the build's ACTUAL total price is, including parent-ignored BOM and Misc entries.
	// Stable, long-lived, lazily-evaluated binding - safe to bind UI to. refresh() marks it dirty via markDirty().
	@JsonIgnore public final DoubleBinding getTotalPrice() { return totalPriceBinding; }

	private double calculateTotalPrice()
	{
		if(price.totalPriceOverriddenProperty.get()) return price.totalPrice.doubleValue();

		double total = price.totalPrice.doubleValue();

		for(BOMEntry bomEntry : bom)
		{
			if(bomEntry.getIgnoreParentQuantityProperty().get()) total += bomEntry.getTotalPriceProperty().get();
		}

		for(MiscEntry miscEntry : misc)
		{
			if(miscEntry.getIgnoreParentQuantityProperty().get()) total += miscEntry.getTotalPriceProperty().get();
		}

		return total;
	}

	public void addConflict(Conflict conflict)
	{
		if(parentTransactionProperty.get() != null) parentTransactionProperty.get().addConflict(conflict);
	}
	
	private void runCatalogDetection()
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
		catalogPrice.set(catalogPrice.get() >= 0 ? -1 : 1);
		runCatalogDetection();
		unoverridenDescriptionProperty.set(calculateDefaultDescription());
	}
	
	@Override
	public String toString()
	{
		return titleProperty.get() + " - " + buildUUID + " (Index In Parent: " + indexInParentWrapper.get() + ")";
	}
}
