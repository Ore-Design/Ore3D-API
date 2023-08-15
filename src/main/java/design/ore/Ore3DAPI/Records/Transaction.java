package design.ore.Ore3DAPI.Records;

import java.util.ArrayList;
import java.util.List;

import design.ore.Ore3DAPI.Records.Subtypes.PricingData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction extends ValueStorageRecord
{
	public Transaction() {}
	
	public Transaction(String id, Customer customer)
	{
		this.id = id;
		this.customer = customer;
		this.builds = new ArrayList<>();
	}
	
	String id;
	PricingData pricing;
	List<Build> builds;
	Customer customer;
}
