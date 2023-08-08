package design.ore.Ore3DAPI.Records;

import java.util.List;

import design.ore.Ore3DAPI.Records.Subtypes.PricingData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Transaction extends ValueStorageRecord
{
	String id;
	PricingData pricing;
	List<Build> builds;
	Customer customer;
}
