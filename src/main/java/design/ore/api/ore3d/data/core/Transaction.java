package design.ore.api.ore3d.data.core;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.api.ore3d.Registry;
import design.ore.api.ore3d.Util;
import design.ore.api.ore3d.Util.Log;
import design.ore.api.ore3d.data.Conflict;
import design.ore.api.ore3d.data.crm.Customer;
import design.ore.api.ore3d.data.interfaces.ValueStorageRecord;
import design.ore.api.ore3d.data.pricing.BOMEntry;
import design.ore.api.ore3d.data.pricing.PricingData;
import design.ore.api.ore3d.data.pricing.RoutingEntry;
import design.ore.api.ore3d.data.wrappers.BuildList;
import design.ore.api.ore3d.jackson.ObservableListSerialization;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

public class Transaction extends ValueStorageRecord
{
	public enum BuildChangeType
	{
		ADDED, REMOVED;
		
		public boolean wasAdded() { return this == ADDED; }
		public boolean wasRemoved() { return this == REMOVED; }
	}
	
	private final Transaction INSTANCE;
	
	@Getter @Setter private String editLog = "";
	public void logChange(String user, String change)
	{
		if(editLog == null) editLog = "\n";
		else if(!editLog.equals("")) editLog += "\n";
		
		editLog += "[" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzzz").format(Date.from(Instant.now())) + "] " + user + " -> " + change;
	}
	
	protected final List<BiConsumer<BuildChangeType, Build>> buildListChangedListeners = new ArrayList<>();
	public final boolean addOnBuildListChangedListener(BiConsumer<BuildChangeType, Build> cnsmr) { return buildListChangedListeners.add(cnsmr); }
	public final boolean removeOnBuildListChangedListener(BiConsumer<BuildChangeType, Build> cnsmr) { return buildListChangedListeners.remove(cnsmr); }
	protected final void fireBuildListChangedEvent(final BuildChangeType changeType, final Build child) { for(BiConsumer<BuildChangeType, Build> cnsmr : buildListChangedListeners) cnsmr.accept(changeType, child); }
	
	public Transaction() { this("0.0.0", null, null, null, null, false, null, false); }
	
	public Transaction(String compatibleVersion, String id, String displayName, Customer customer, PricingData pricing, boolean canGenerateWorkOrders, String lockedBy, boolean expired)
	{
		this(compatibleVersion, id, displayName, customer, pricing, canGenerateWorkOrders, lockedBy, new BuildList(), expired);
	}
	
	public Transaction(String compatibleVersion, String id, String displayName, Customer customer, PricingData pricing, boolean canGenerateWorkOrders, String lockedBy, BuildList blds, boolean expired)
	{
		INSTANCE = this;
		
		this.id = id;
		this.customer = customer;
		this.pricing = pricing;
		this.canGenerateWorkOrders = canGenerateWorkOrders;
		this.displayName = displayName;
		this.builds = new BuildList();
		this.lockedBy = lockedBy;
		this.compatibleVersion = compatibleVersion;
		this.expired = expired;
		
		tags = FXCollections.observableArrayList();
		
		addOnBuildListChangedListener((ct, b) ->
		{
			conflicts.clear();
			
			onBuildListChanged(ct, b);
			
			for(Build bld : builds) { bld.refresh(); }
		});
		
		builds.addListener(new ListChangeListener<>()
		{
			@Override
			public void onChanged(Change<? extends Build> c)
			{
				while(c.next())
				{
					for(Build b : c.getAddedSubList())
					{
						b.getParentTransactionProperty().set(INSTANCE);
						cleanseAndRebindBuildData(b);
						
						b.refresh();
						fireBuildListChangedEvent(BuildChangeType.ADDED, b);
					}
					for(Build rb : c.getRemoved())
					{
						fireBuildListChangedEvent(BuildChangeType.REMOVED, rb);
						
						rb.getParentTransactionProperty().set(null);
					}
				}
			}
		});
		
		builds.setAll(blds);
		
		conflicts.clear();
	}
	
	protected final void onBuildListChanged(BuildChangeType ct, Build b)
	{
		if(ct.wasAdded())
		{
			while(true)
			{
				boolean duplicateUIDFound = false;
				for(Build bld : this.getAllBuildsIncludingChildren())
				{
					if(bld.equals(b)) continue;
					
					if(bld.getBuildUUID() == b.getBuildUUID())
					{
						duplicateUIDFound = true;
						break;
					}
				}
				
				if(duplicateUIDFound) b.regenerateBuildUUID();
				else break;
			}
		}
		
		for(Build cb : b.getChildBuilds())
		{
			onBuildListChanged(ct, cb);
		}
	}
	
	private void cleanseAndRebindBuildData(Build b)
	{
		if(isExpired()) return;
		
		for(Build cb : b.getChildBuilds()) { cleanseAndRebindBuildData(cb); }
		
		if(!b.getParentTransactionProperty().isBound() && !b.getParentTransactionProperty().get().equals(this)) b.getParentTransactionProperty().set(this);
		
		List<BOMEntry> bomToAdd = new ArrayList<BOMEntry>();
		for(BOMEntry bom : b.getBom())
		{
			BOMEntry match = Registry.getBOMEntries().get(bom.getId());
			if(match != null) bomToAdd.add(Util.duplicateBOMWithPricing(this, b, match, bom));
			else { Util.Log.getLogger().warn("Data has changed since this record was last loaded! BOM entry " + bom.getId() + " no longer exists in loaded databases!"); }
		}
		
		b.getBom().clear();
		b.getBom().addAll(bomToAdd);

		List<RoutingEntry> routingToAdd = new ArrayList<RoutingEntry>();
		for(RoutingEntry r : b.getRoutings())
		{
			RoutingEntry match = Registry.getRoutingEntries().get(r.getId());
			if(match != null) routingToAdd.add(Util.duplicateRoutingWithPricing(this, b, match, r));
			else { Util.Log.getLogger().warn("Data has changed since this record was last loaded! Routing entry " + r.getId() + " no longer exists in loaded databases!"); }
		}
		
		b.getRoutings().clear();
		b.getRoutings().addAll(routingToAdd);
		
		b.setDirty();
	}
	
	@Getter @Setter String compatibleVersion;
	@Getter @Setter String id;
	@Getter @Setter String displayName;
	@Setter PricingData pricing;
    public PricingData getPricing()
    {
        if(pricing == null) pricing = Registry.getPricingDataSupplier().get();
    	return pricing;
    }
	@Getter @JsonMerge final BuildList builds;
	@Getter @Setter Customer customer;
	@Setter boolean canBeDuplicated;
	public boolean canBeDuplicated() { return canBeDuplicated; }
	@Setter boolean canGenerateWorkOrders;
	public boolean canGenerateWorkOrders() { return canGenerateWorkOrders; }
	@Getter @Setter String lockedBy;
	@Getter @Setter boolean expired;
	@Getter @Setter boolean reorderAllowed = true;

	@JsonSerialize(using = ObservableListSerialization.TagList.Serializer.class)
	@JsonDeserialize(using = ObservableListSerialization.TagList.Deserializer.class)
	@Getter ObservableList<Tag> tags;
	
	@JsonIgnore
	private final ReadOnlyListWrapper<Conflict> conflicts = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
	@JsonIgnore
	public final ReadOnlyListProperty<Conflict> getConflictsReadOnly() { return conflicts.getReadOnlyProperty(); }
	@JsonIgnore protected void addConflict(Conflict conflict)
	{
		if(conflict == null)
		{
			Log.getLogger().warn("Can't add null conflict to transaction!");
			return;
		}
		
		Optional<Conflict> duplicate = conflicts.stream().filter(cnf -> cnf != null && cnf.equals(conflict)).findFirst();
		if(duplicate.isPresent()) conflicts.remove(duplicate.get());
		conflicts.add(conflict);
	}
	public void invalidateConflicts() { conflicts.set(conflicts); }
	protected void removeConflictsForBuild(int uid)
	{
		List<Conflict> matching = conflicts.stream().filter(c -> c != null && c.getBuildUID() == uid).toList();
		conflicts.removeAll(matching);
	}
	public IntegerBinding warningConflictCount() { return Bindings.createIntegerBinding(() -> conflicts.stream().filter(cnf -> cnf.isWarning()).toList().size(), conflicts); }
	public IntegerBinding errorConflictCount() { return Bindings.createIntegerBinding(() -> conflicts.stream().filter(cnf -> !cnf.isWarning()).toList().size(), conflicts); }
	public BooleanBinding buildHasWarningConflicts(Build b)
	{ return Bindings.createBooleanBinding(() -> conflicts.stream().anyMatch(cnf -> cnf.getBuildUID() == b.getBuildUUID() && cnf.isWarning()), conflicts); }
	public BooleanBinding buildHasErrorConflicts(Build b)
	{ return Bindings.createBooleanBinding(() -> conflicts.stream().anyMatch(cnf -> cnf.getBuildUID() == b.getBuildUUID() && !cnf.isWarning()), conflicts); }
	public BooleanBinding buildHasConflicts(Build b) { return buildHasWarningConflicts(b).or(buildHasErrorConflicts(b)); }
	public List<Conflict> getConflictsForBuild(Build b)
	{ return conflicts.stream().filter(cnf -> cnf.getBuildUID() == b.getBuildUUID()).toList(); }
	
	@JsonIgnore
	public Map<Integer, Build> getAllBuildsByUID()
	{
		Map<Integer, Build> allBuilds = new HashMap<>();
		for(Build b : builds)
		{
			allBuilds.put(b.getBuildUUID(), b);
			allBuilds.putAll(b.getAllChildBuildsByUID());
		}
		return allBuilds;
	}
	
	@JsonIgnore
	public List<Build> getAllBuildsIncludingChildren()
	{
		List<Build> allBuilds = new ArrayList<>();
		for(Build b : builds) { allBuilds.addAll(getAllBuildsRecursive(b)); }
		return allBuilds;
	}
	
	@JsonIgnore
	private List<Build> getAllBuildsRecursive(Build parent)
	{
		List<Build> allBuilds = new ArrayList<>();
		
		allBuilds.add(parent);
		for(Build b : parent.getChildBuilds()) { allBuilds.addAll(getAllBuildsRecursive(b)); }
		
		return allBuilds;
	}
	
	@JsonIgnore
	public int getGreatestChildDepth()
	{
		int depth = 0;
		for(Build b : getAllBuildsIncludingChildren())
		{
			if(b.getChildDepth().get() > depth) depth = b.getChildDepth().get();
		}
		return depth;
	}
	
	@Override
	public String toString() { return "[ Transaction: " + displayName + " (" + id + ") - Created Version: " + compatibleVersion + " ]"; }
}
