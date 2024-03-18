package design.ore.Ore3DAPI.DataTypes.CRM;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.DataTypes.Conflict;
import design.ore.Ore3DAPI.DataTypes.Build.Build;
import design.ore.Ore3DAPI.DataTypes.Build.Tag;
import design.ore.Ore3DAPI.DataTypes.Interfaces.Conflictable;
import design.ore.Ore3DAPI.DataTypes.Interfaces.ValueStorageRecord;
import design.ore.Ore3DAPI.DataTypes.Pricing.PricingData;
import design.ore.Ore3DAPI.DataTypes.Wrappers.BuildList;
import design.ore.Ore3DAPI.Jackson.ObservableListSerialization;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

public class Transaction extends ValueStorageRecord implements Conflictable
{
	public Transaction() { this("0.0.0", null, null, null, null, false, null, false); }
	
	public Transaction(String compatibleVersion, String id, String displayName, Customer customer, PricingData pricing, boolean canGenerateWorkOrders, String lockedBy, boolean expired)
	{
		this(compatibleVersion, id, displayName, customer, pricing, canGenerateWorkOrders, lockedBy, new BuildList(), expired);
	}
	
	public Transaction(String compatibleVersion, String id, String displayName, Customer customer, PricingData pricing, boolean canGenerateWorkOrders, String lockedBy, BuildList blds, boolean expired)
	{
		this.id = id;
		this.customer = customer;
		this.pricing = pricing;
		this.canGenerateWorkOrders = canGenerateWorkOrders;
		this.displayName = displayName;
		this.builds = new BuildList();
		this.lockedBy = lockedBy;
		this.compatibleVersion = compatibleVersion;
		this.expired = expired;
		this.
		
		conflicts = FXCollections.observableArrayList();
		tags = FXCollections.observableArrayList();
		
		for(Build newBuild : builds) newBuild.getConflicts().addListener((ListChangeListener.Change<? extends Conflict> c) -> resetConflictList(builds));
		
		this.builds.addListener((ListChangeListener.Change<? extends Build> c) ->
		{
			resetConflictList(c.getList());
			while(c.next())
			{
				for(Build b : c.getAddedSubList())
				{
					b.getParentTransactionProperty().set(this);
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
					b.registerDirtyListenerEvent("CONFLICT_DETECTION", (obs, oldVal, newVal) -> { if(!newVal) resetConflictList(builds); });
//					b.getConflicts().addListener((ListChangeListener.Change<?> ch) -> resetConflictList(builds));
				}
				for(Build rb : c.getRemoved())
				{
					rb.getParentTransactionProperty().set(null);
					rb.unregisterDirtyListenerEvent("CONFLICT_DETECTION");
				}
			}
		});
		
		this.builds.setAll(blds);
		
		resetConflictList(builds);
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
	@Getter @Setter BuildList builds;
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
}
