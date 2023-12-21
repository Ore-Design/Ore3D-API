package design.ore.Ore3DAPI.DataTypes.Interfaces;

import design.ore.Ore3DAPI.DataTypes.CRM.Transaction;
import design.ore.Ore3DAPI.DataTypes.Wrappers.UpdatePacket;
import javafx.beans.binding.BooleanBinding;
import lombok.Getter;

public abstract class CustomSaveCycleReference
{	
	@Getter final BooleanBinding trigger;
	
	public CustomSaveCycleReference(BooleanBinding trigger) { this.trigger = trigger; }
	
	public abstract UpdatePacket generateUpdatePacket(Transaction transaction);
}
