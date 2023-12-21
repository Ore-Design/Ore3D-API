package design.ore.Ore3DAPI.DataTypes.Interfaces;

import design.ore.Ore3DAPI.DataTypes.CRM.Transaction;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.layout.Pane;
import lombok.Getter;

public abstract class CustomButtonReference
{
	@Getter final Transaction transaction;
	@Getter final Pane popupOnAction;
	
	public CustomButtonReference(Transaction transaction, Pane popupOnAction)
	{
		this.transaction = transaction;
		this.popupOnAction = popupOnAction;
	}
	
	public final Pane onAction()
	{
		if(popupOnAction != null) return popupOnAction;
		else onClick();
		return null;
	}
	
	public abstract void onClick();
	public abstract BooleanBinding createDisableBinding();
}
