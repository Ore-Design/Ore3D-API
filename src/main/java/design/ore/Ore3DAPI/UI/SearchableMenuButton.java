package design.ore.Ore3DAPI.UI;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

public class SearchableMenuButton extends TextField
{
	private final ContextMenu contextMenu = new ContextMenu();
	public ObservableList<MenuItem> getItems() { return contextMenu.getItems(); }
	
	public SearchableMenuButton() { this("Menu"); }
	
	public SearchableMenuButton(String title)
	{
		
		getStyleClass().add("searchable-menu-button");
		setPromptText("Search Items...");
		textProperty().addListener((obs, oldVal, newVal) -> filterOptions(newVal));
		setContextMenu(contextMenu);
		
		contextMenu.prefWidthProperty().bind(this.widthProperty());
		contextMenu.setMaxWidth(USE_PREF_SIZE);
		
		focusedProperty().addListener((obs, oldVal, newVal) ->
		{
			if(newVal) showContextMenu();
			else if(!newVal) contextMenu.hide();
		});
	}
	
	private void filterOptions(String filter)
	{
		if(filter!= null && !filter.equals("")) showContextMenu();
		
		for(MenuItem mi : getItems())
		{
			menuItemFilterRecursive(mi, filter);
		}
	}
	
	private void showContextMenu()
	{
		Bounds bounds = this.getBoundsInLocal();
        Bounds screenBounds = this.localToScreen(bounds);
        int x = (int) screenBounds.getMinX();
        int y = (int) (screenBounds.getMinY() + getHeight());
        
		contextMenu.show(this, x, y);
	}
	
	private boolean menuItemFilterRecursive(MenuItem item, String filter)
	{
		if(item instanceof Menu)
		{
			Menu menu = (Menu) item;
			
			boolean childMatches = false;
			for(MenuItem cmi : menu.getItems())
			{
				if(menuItemFilterRecursive(cmi, filter)) childMatches = true;
			}
			item.setVisible(childMatches);
			return childMatches;
		}
		else if((filter == null || filter.equals("")) || item.getText().toLowerCase().contains(filter.toLowerCase()))
		{
			item.setVisible(true);
			return true;
		}
			
		item.setVisible(false);
		return false;
	}
}
