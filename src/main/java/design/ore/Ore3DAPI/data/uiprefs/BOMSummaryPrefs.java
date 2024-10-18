package design.ore.Ore3DAPI.data.uiprefs;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BOMSummaryPrefs
{
	@Getter @JsonIgnore SimpleBooleanProperty viewShortNameProperty = new SimpleBooleanProperty(true),
		viewLongNameProperty = new SimpleBooleanProperty(true),
		viewUOMProperty = new SimpleBooleanProperty(true),
		viewCostPerQuantityProperty = new SimpleBooleanProperty(true),
		viewIsCustomEntryProperty = new SimpleBooleanProperty(true),
		viewQuantityProperty = new SimpleBooleanProperty(true),
		viewOverriddenQuantityProperty = new SimpleBooleanProperty(true),
		viewMarginProperty = new SimpleBooleanProperty(true),
		viewOverriddenMarginProperty = new SimpleBooleanProperty(true),
		viewIsIgnoreParentQuantityProperty = new SimpleBooleanProperty(true),
		viewTotalCostProperty = new SimpleBooleanProperty(true),
		viewUnitCostProperty = new SimpleBooleanProperty(true),
		viewTotalPriceProperty = new SimpleBooleanProperty(true),
		viewUnitPriceProperty = new SimpleBooleanProperty(true);
	
	public boolean getViewShortName() { return viewShortNameProperty.get(); }
	public boolean getViewLongName() { return viewLongNameProperty.get(); }
	public boolean getViewUOM() { return viewUOMProperty.get(); }
	public boolean getViewCostPerQuantity() { return viewCostPerQuantityProperty.get(); }
	public boolean getViewIsCustomEntry() { return viewIsCustomEntryProperty.get(); }
	public boolean getViewQuantity() { return viewQuantityProperty.get(); }
	public boolean getViewOverriddenQuantity() { return viewOverriddenQuantityProperty.get(); }
	public boolean getViewMargin() { return viewMarginProperty.get(); }
	public boolean getViewOverriddenMargin() { return viewOverriddenMarginProperty.get(); }
	public boolean getViewIsIgnoreParentQuantity() { return viewIsIgnoreParentQuantityProperty.get(); }
	public boolean getViewTotalCost() { return viewTotalCostProperty.get(); }
	public boolean getViewUnitCost() { return viewUnitCostProperty.get(); }
	public boolean getViewTotalPrice() { return viewTotalPriceProperty.get(); }
	public boolean getViewUnitPrice() { return viewUnitPriceProperty.get(); }
	
	public void setViewShortName(boolean view) { viewShortNameProperty.setValue(view); }
	public void setViewLongName(boolean view) { viewLongNameProperty.setValue(view); }
	public void setViewUOM(boolean view) { viewUOMProperty.setValue(view); }
	public void setViewCostPerQuantity(boolean view) { viewCostPerQuantityProperty.setValue(view); }
	public void setViewIsCustomEntry(boolean view) { viewIsCustomEntryProperty.setValue(view); }
	public void setViewQuantity(boolean view) { viewQuantityProperty.setValue(view); }
	public void setViewOverriddenQuantity(boolean view) { viewOverriddenQuantityProperty.setValue(view); }
	public void setViewMargin(boolean view) { viewMarginProperty.setValue(view); }
	public void setViewOverriddenMargin(boolean view) { viewOverriddenMarginProperty.setValue(view); }
	public void setViewIsIgnoreParentQuantity(boolean view) { viewIsIgnoreParentQuantityProperty.setValue(view); }
	public void setViewTotalCost(boolean view) { viewTotalCostProperty.setValue(view); }
	public void setViewUnitCost(boolean view) { viewUnitCostProperty.setValue(view); }
	public void setViewTotalPrice(boolean view) { viewTotalPriceProperty.setValue(view); }
	public void setViewUnitPrice(boolean view) { viewUnitPriceProperty.setValue(view); }
}
