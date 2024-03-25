package design.ore.Ore3DAPI.DataTypes.Interfaces;

import design.ore.Ore3DAPI.DataTypes.CRM.Transaction;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public abstract class CustomButtonReference
{	
	public final Pane onAction(Transaction transaction)
	{
		Pane popup = generatePopup(transaction);
		if(popup != null) return popup;
		else onClick(transaction);
		return null;
	}
	
	public abstract Pane generatePopup(Transaction transaction);
	public abstract String getStageTitle(Transaction transaction);
	public abstract void onClick(Transaction transaction);
	public abstract BooleanBinding createDisableBinding(Transaction transaction);
	public abstract Image getButtonIcon();
	public abstract String getButtonTooltip();
	public abstract boolean useDefaultStyle();
}
