package design.ore.Ore3DAPI.DataTypes.Records;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.Ore3DAPI.DataTypes.BuildList;
import design.ore.Ore3DAPI.DataTypes.Conflict;
import design.ore.Ore3DAPI.DataTypes.Interfaces.Conflictable;
import design.ore.Ore3DAPI.DataTypes.Records.Pricing.PricingData;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction extends ValueStorageRecord implements Conflictable
{
	public Transaction() { conflicts = FXCollections.observableArrayList(); }
	
	@JsonIgnore @Getter private ObservableList<Conflict> conflicts;
	
	public Transaction(String id, String displayName, Customer customer, PricingData pricing, boolean isSalesOrder, String lockedBy)
	{
		this(id, displayName, customer, pricing, isSalesOrder, lockedBy, new BuildList());
	}
	
	public Transaction(String id, String displayName, Customer customer, PricingData pricing, boolean isSalesOrder, String lockedBy, BuildList builds)
	{
		this.id = id;
		this.customer = customer;
		this.pricing = pricing;
		this.salesOrder = isSalesOrder;
		this.displayName = displayName;
		this.builds = builds;
		this.lockedBy = lockedBy;
		
		conflicts = FXCollections.observableArrayList();
		
		for(Build newBuild : builds) newBuild.getConflicts().addListener((ListChangeListener.Change<? extends Conflict> c) -> resetConflictList(builds));
		
		builds.addListener((ListChangeListener.Change<? extends Build> c) ->
		{
			resetConflictList(c.getList());
			c.next();
			for(Build b : c.getAddedSubList())
			{
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
				b.getConflicts().addListener((ListChangeListener.Change<?> ch) -> resetConflictList(builds));
			}
		});
		
		resetConflictList(builds);
	}
	
	private void resetConflictList(List<? extends Build> blds)
	{
		conflicts.clear();
		for(Build b : blds) conflicts.setAll(FXCollections.concat(conflicts, b.getConflicts()));
	}
	
	String id;
	String displayName;
	PricingData pricing;
	BuildList builds;
	Customer customer;
	boolean salesOrder;
	String lockedBy;

	@Override
	public void addConflict(Conflict conflict) { throw new UnsupportedOperationException("Add conflicts to individual children, not the transaction as a whole!"); }

	@Override
	public void clearConflicts() { throw new UnsupportedOperationException("Clear conflicts from individual children, not the transaction as a whole!"); }
}
