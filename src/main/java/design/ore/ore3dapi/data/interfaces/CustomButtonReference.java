package design.ore.ore3dapi.data.interfaces;

import design.ore.ore3dapi.data.core.Transaction;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public interface CustomButtonReference
{
	public Pane generatePopup(Transaction transaction);
	public String getStageTitle(Transaction transaction);
	public void onClick(Transaction transaction);
	public BooleanBinding createDisableBinding(Transaction transaction);
	public Image getButtonIcon();
	public String getButtonTooltip();
	public boolean useDefaultStyle();
}
