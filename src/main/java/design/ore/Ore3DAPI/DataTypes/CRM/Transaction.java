package design.ore.Ore3DAPI.DataTypes.CRM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Registry;
import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.DataTypes.Conflict;
import design.ore.Ore3DAPI.DataTypes.Build.Build;
import design.ore.Ore3DAPI.DataTypes.Build.Tag;
import design.ore.Ore3DAPI.DataTypes.Interfaces.Conflictable;
import design.ore.Ore3DAPI.DataTypes.Interfaces.ValueStorageRecord;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.PricingData;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Wrappers.BuildList;
import design.ore.Ore3DAPI.Jackson.ObservableListSerialization;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

public class Transaction extends ValueStorageRecord implements Conflictable
{
	private final Transaction INSTANCE;
	
	protected final List<Consumer<Build>> childRemovedListeners = new ArrayList<>();
	protected final List<Consumer<Build>> childAddedListeners = new ArrayList<>();
	public boolean addOnChildRemovedListener(Consumer<Build> cnsmr) { return childRemovedListeners.add(cnsmr); }
	public boolean removeOnChildRemovedListener(Consumer<Build> cnsmr) { return childRemovedListeners.remove(cnsmr); }
	public boolean addOnChildAddedListener(Consumer<Build> cnsmr) { return childAddedListeners.add(cnsmr); }
	public boolean removeOnChildAddedListener(Consumer<Build> cnsmr) { return childAddedListeners.remove(cnsmr); }
	public void fireChildRemovedEvent(final Build child) { for(Consumer<Build> cnsmr : childRemovedListeners) cnsmr.accept(child); }
	public void fireChildAddedEvent(final Build child) { for(Consumer<Build> cnsmr : childAddedListeners) cnsmr.accept(child); }
	
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
		
		conflicts = FXCollections.observableArrayList();
		tags = FXCollections.observableArrayList();
		
		for(Build newBuild : builds) newBuild.getConflicts().addListener((ListChangeListener.Change<? extends Conflict> c) -> resetConflictList(builds));
		
		ChangeListener<Boolean> conflictsListListener = (obs, oldVal, newVal) -> { if(!newVal) resetConflictList(builds); };
		
		builds.addListener(new ListChangeListener<>()
		{
			@Override
			public void onChanged(Change<? extends Build> c)
			{
				while(c.next())
				{
					resetConflictList(c.getList());
					for(Build b : c.getAddedSubList())
					{
						b.getParentTransactionProperty().set(INSTANCE);
						while(true)
						{
							boolean duplicateUIDFound = false;
							for(Build bld : c.getList())
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
						b.getIsDirtyProperty().addListener(conflictsListListener);
						
						cleanseAndRebindBuildData(b);
					}
					for(Build rb : c.getRemoved())
					{
						rb.getParentTransactionProperty().set(null);
						rb.getIsDirtyProperty().removeListener(conflictsListListener);
					}
				}
			}
		});
		
		builds.setAll(blds);
		
		resetConflictList(builds);
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
	
	private void resetConflictList(List<? extends Build> blds)
	{
		conflicts.clear();
		for(Build b : blds) concatConflictsRecursive(conflicts, b);
	}
	
	private void concatConflictsRecursive(ObservableList<Conflict> conf, Build b)
	{
		conf.setAll(FXCollections.concat(conf, b.getConflicts()));
		for(Build cb : b.getChildBuilds()) concatConflictsRecursive(conf, cb);
	}
	
	@Getter @Setter String compatibleVersion;
	@Getter @Setter String id;
	@Getter @Setter String displayName;
	@Getter @Setter PricingData pricing;
	@Getter final BuildList builds;
	@Getter @Setter Customer customer;
	@Setter boolean canBeDuplicated;
	public boolean canBeDuplicated() { return canBeDuplicated; }
	@Setter boolean canGenerateWorkOrders;
	public boolean canGenerateWorkOrders() { return canGenerateWorkOrders; }
	@Getter @Setter String lockedBy;
	@Getter @Setter boolean expired;

	@JsonSerialize(using = ObservableListSerialization.TagList.Serializer.class)
	@JsonDeserialize(using = ObservableListSerialization.TagList.Deserializer.class)
	@Getter ObservableList<Tag> tags;
	
	@JsonIgnore @Getter private ObservableList<Conflict> conflicts;
	
	@JsonIgnore
	public Map<Integer, Build> getAllBuildsByUID()
	{
		Map<Integer, Build> allBuilds = new HashMap<>();
		for(Build b : builds)
		{
			allBuilds.put(b.getBuildUUID(), b);
			allBuilds.putAll(b.getChildBuildsMap());
		}
		return allBuilds;
	}

	@Override
	public void addConflict(Conflict conflict) { throw new UnsupportedOperationException("Add conflicts to individual children, not the transaction as a whole!"); }

	@Override
	public void clearConflicts() { throw new UnsupportedOperationException("Clear conflicts from individual children, not the transaction as a whole!"); }
	
	@Override
	public String toString() { return "[ Transaction: " + displayName + " (" + id + ") - Created Version: " + compatibleVersion + " ]"; }
}
