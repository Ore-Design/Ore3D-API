package design.ore.Ore3DAPI.Records;

import java.util.ArrayList;
import java.util.List;

import design.ore.Ore3DAPI.Records.Subtypes.Pricing.PricingData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction extends ValueStorageRecord
{
	public Transaction() {}
	
	public Transaction(String id, String displayName, Customer customer, PricingData pricing, boolean isSalesOrder)
	{
		this(id, displayName, customer, pricing, isSalesOrder, new ArrayList<Build>());
	}
	
	public Transaction(String id, String displayName, Customer customer, PricingData pricing, boolean isSalesOrder, List<Build> builds)
	{
		this.id = id;
		this.customer = customer;
		this.pricing = pricing;
		this.salesOrder = isSalesOrder;
		this.displayName = displayName;
		this.builds = builds;
	}
	
	String id;
	String displayName;
	PricingData pricing;
	List<Build> builds;
	Customer customer;
	boolean salesOrder;
}
