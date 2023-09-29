package design.ore.Ore3DAPI.DataTypes.Records;

import design.ore.Ore3DAPI.DataTypes.Records.Pricing.PricingData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction extends ValueStorageRecord
{
	public Transaction() {}
	
	public Transaction(String id, String displayName, Customer customer, PricingData pricing, boolean isSalesOrder, String lockedBy)
	{
		this(id, displayName, customer, pricing, isSalesOrder, lockedBy, FXCollections.observableArrayList());
	}
	
	public Transaction(String id, String displayName, Customer customer, PricingData pricing, boolean isSalesOrder, String lockedBy, ObservableList<Build> builds)
	{
		this.id = id;
		this.customer = customer;
		this.pricing = pricing;
		this.salesOrder = isSalesOrder;
		this.displayName = displayName;
		this.builds = builds;
		this.lockedBy = lockedBy;
	}
	
	String id;
	String displayName;
	PricingData pricing;
	ObservableList<Build> builds;
	Customer customer;
	boolean salesOrder;
	String lockedBy;
}
