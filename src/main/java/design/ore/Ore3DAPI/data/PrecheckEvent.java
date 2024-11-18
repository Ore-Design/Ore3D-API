package design.ore.Ore3DAPI.data;

import design.ore.Ore3DAPI.data.core.Transaction;
import lombok.Getter;

public class PrecheckEvent
{
	@Getter final Transaction transaction;
	@Getter private boolean interrupted;
	
	public PrecheckEvent(Transaction transaction)
	{
		this.transaction = transaction;
		interrupted = false;
	}
	
	public void interrupt() { interrupted = true; }
}
