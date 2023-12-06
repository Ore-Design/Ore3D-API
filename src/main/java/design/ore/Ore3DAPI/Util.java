package design.ore.Ore3DAPI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import design.ore.Ore3DAPI.DataTypes.Build.Build;
import design.ore.Ore3DAPI.DataTypes.CRM.Transaction;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMPricing;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingPricing;
import design.ore.Ore3DAPI.UI.PopoutPane;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextFormatter;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;

public class Util
{
	public class Colors
	{
		@Getter @Setter static Color foreground;
		@Getter @Setter static Color secondaryForeground;
		@Getter @Setter static Color tertiaryForeground;
		@Getter @Setter static Color transparentForeground;
		@Getter @Setter static Color secondaryTransparentForeground;
		@Getter @Setter static Color tertiaryTransparentForeground;
		@Getter @Setter static Color background;
		@Getter @Setter static Color secondaryBackground;
		@Getter @Setter static Color transparentBackground;
		@Getter @Setter static Color dimBackground;
		@Getter @Setter static Color accent;
	}
	
	public class Log
	{
		@Getter private static Logger logger = null;
//		public static void debug(String message) { if(LOG != null) LOG.debug(message); }
//		public static void info(String message) { if(LOG != null) LOG.info(message); }
//		public static void warn(String message) { if(LOG != null) LOG.warn(message); }
//		public static void error(String message) { if(LOG != null) LOG.error(message); }
		public static void registerLogger(Logger log) { if(logger == null) { logger = log; } else { logger.warn("Someone attempted to register a different logger, but it's locked!"); } }
		public static Appender<ILoggingEvent> getAppender(String name) { if(logger != null) { return logger.getAppender(name); } else return null; }
	}
	
	public class Mapper
	{
		@Getter private static ObjectMapper mapper = null;
		public static void registerMapper(ObjectMapper map) { if(mapper == null) { mapper = map; } else { Log.logger.warn("Someone attempted to register a different logger, but it's locked!"); } }
	}
	
	@Getter private static final Image brokenChainIcon = new Image("ui/icons/BrokenChainIcon.png");
	@Getter private static final Image chainIcon = new Image("ui/icons/ChainIcon.png");
	@Getter private static final Image xIcon = new Image("ui/icons/XIcon.png");
	
	@Getter private static final List<ClassLoader> registeredClassLoaders = new ArrayList<ClassLoader>();
	public static void registerClassLoader(ClassLoader cl) { registeredClassLoaders.add(cl); }
	
	@Getter private static final Map<String, Map<Integer, String>> registeredIntegerStringMaps = new HashMap<>();
	public static void registerIntStringMap(String mapID, Map<Integer, String> map)
	{
		if(registeredIntegerStringMaps.containsKey(mapID)) Log.getLogger().warn("A map with the ID " + mapID + " has already been registered! Overriding...");
		registeredIntegerStringMaps.put(mapID, map);
	}
	
//	private static StackPane
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
	{
	    List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
	    list.sort(Entry.comparingByValue());
	
	    Map<K, V> result = new LinkedHashMap<>();
	    for (Entry<K, V> entry : list) { result.put(entry.getKey(), entry.getValue()); }
	
	    return result;
	}

//	public static String stackTraceArrayToString(Exception e)
//	{
//		StringWriter sw = new StringWriter();
//		PrintWriter pw = new PrintWriter(sw);
//		e.printStackTrace(pw);
//		return sw.toString();
//	}

	public static String stackTraceArrayToString(Throwable e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
	public static TextFormatter<?> getDecimalFormatter(int decimalPlaces)
	{
		return new TextFormatter<>((UnaryOperator<TextFormatter.Change>) change ->
		{ return Pattern.compile("\\d*|\\d+\\.\\d{0," + decimalPlaces + "}").matcher(change.getControlNewText()).matches() ? change : null; });
	}
	
	public static TextFormatter<?> getIntegerFormatter()
	{
		return new TextFormatter<>((UnaryOperator<TextFormatter.Change>) change ->
		{ return Pattern.compile("\\d*").matcher(change.getControlNewText()).matches() ? change : null; });
	}
	
	public static TextFormatter<?> get0to99IntegerFormatter()
	{
		return new TextFormatter<>((UnaryOperator<TextFormatter.Change>) change ->
		{ return Pattern.compile("(^$)|(^(0?[1-9]|[1-9][0-9])$)").matcher(change.getControlNewText()).matches() ? change : null; });
	}
	
	public static double squareInchesToSquareFeet(double squareInches) { return squareInches / 144.0; }
	
	public static BOMEntry duplicateBOMWithPricing(Transaction transaction, Build parent, BOMEntry entry, boolean isCustom)
	{
		Optional<BOMPricing> pricing = transaction.getPricing().getBom().stream().filter(bp -> bp.getInternalID().equals(entry.getId())).findFirst();
		if(pricing.isPresent()) return entry.duplicate(pricing.get().getCostPerUnit(), 1, parent.getQuantity().getIntProperty(), isCustom);
		else return entry.duplicate(1, parent.getQuantity().getIntProperty(), isCustom);
	}
	
	public static BOMEntry duplicateBOMWithPricing(Transaction transaction, Build parent, BOMEntry entry, BOMEntry originalEntry)
	{
		Optional<BOMPricing> pricing = transaction.getPricing().getBom().stream().filter(bp -> bp.getInternalID().equals(entry.getId())).findFirst();
		BOMEntry newEntry = null;
		if(pricing.isPresent()) newEntry = entry.duplicate(pricing.get().getCostPerUnit(), originalEntry.getQuantityProperty().get(), parent.getQuantity().getIntProperty(), originalEntry.getCustomEntryProperty().get());
		else newEntry = entry.duplicate(originalEntry.getQuantityProperty().get(), parent.getQuantity().getIntProperty(), originalEntry.getCustomEntryProperty().get());
		
		if(originalEntry.getQuantityOverriddenProperty().get()) newEntry.getOverridenQuantityProperty().set(originalEntry.getOverridenQuantityProperty().get());
		if(originalEntry.getMarginOverriddenProperty().get()) newEntry.getOverridenMarginProperty().set(originalEntry.getOverridenMarginProperty().get());
		
		return newEntry;
	}
	
	public static RoutingEntry duplicateRoutingWithPricing(Transaction transaction, Build parent, RoutingEntry entry)
	{
		Optional<RoutingPricing> pricing = transaction.getPricing().getRoutings().stream().filter(bp -> bp.getId().equals(entry.getId())).findFirst();
		if(pricing.isPresent()) return entry.duplicate(pricing.get().getCostPerMinute(), 1d, parent.getQuantity().getIntProperty());
		else return entry.duplicate(null, 1d, parent.getQuantity().getIntProperty());
	}
	
	public static class UI
	{
		public static Alert confirm(String title, String message, Stage owner)
		{
			Alert confirm = new Alert(AlertType.CONFIRMATION);
			confirm.initOwner(owner);
			confirm.setTitle("Confirm");
			confirm.setHeaderText(title);
			confirm.setContentText(message);
			confirm.getDialogPane().getStylesheets().add("stylesheets/dark.css");
			confirm.initStyle(StageStyle.UNDECORATED);
			confirm.setGraphic(null);
			
			return confirm;
		}
		
		public static Alert info(String title, String message, Stage owner)
		{
			Alert info = new Alert(AlertType.INFORMATION);
			info.initOwner(owner);
			info.setTitle("Confirm");
			info.setHeaderText(title);
			info.setContentText(message);
			info.getDialogPane().getStylesheets().add("stylesheets/dark.css");
			info.initStyle(StageStyle.UNDECORATED);
			info.setGraphic(null);
			
			return info;
		}
		
		public static Alert warn(String title, String message, Stage owner)
		{
			Alert warn = new Alert(AlertType.WARNING);
			warn.initOwner(owner);
			warn.setTitle("Confirm");
			warn.setHeaderText(title);
			warn.setContentText(message);
			warn.getDialogPane().getStylesheets().add("stylesheets/dark.css");
			warn.initStyle(StageStyle.UNDECORATED);
			warn.setGraphic(null);
			
			return warn;
		}
		
		public static Alert error(String title, String message, Stage owner)
		{
			Alert error = new Alert(AlertType.ERROR);
			error.initOwner(owner);
			error.setTitle("Error");
			error.setHeaderText(title);
			error.setContentText(message);
			error.getDialogPane().getStylesheets().add("stylesheets/dark.css");
			error.initStyle(StageStyle.UNDECORATED);
			error.setGraphic(null);
			
			return error;
		}
		
		public static ImageView colorize(ImageView img, Color color)
		{

			ImageView checkClip = new ImageView(img.getImage());
			img.setClip(checkClip);
			img.setPreserveRatio(true);
			checkClip.setPreserveRatio(true);
			checkClip.fitWidthProperty().bind(img.fitWidthProperty());
			
			ColorAdjust monochrome = new ColorAdjust();
	        monochrome.setSaturation(-1.0);
	        Blend colorify = new Blend(BlendMode.MULTIPLY, monochrome, new ColorInput( 0, 0, img.getImage().getWidth(), img.getImage().getHeight(), color));
	        img.setEffect(colorify);
	        
	        return img;
		}
		
		public static void checkboxMatchSize(CheckBox box)
		{
			box.heightProperty().addListener(l -> ((Region) box.lookup(".mark")).setPadding(new Insets((box.getHeight()/2) - 7)));
		}
		
		private static final Map<String, Pane> popoutAreas = new HashMap<>();
		public static void registerPopoutArea(String ID, Pane pane)
		{
			if(popoutAreas.containsKey(ID)) throw new IllegalArgumentException("Popout area with ID " + ID + " has already been registered!");
			else popoutAreas.put(ID, pane);
		}
		public static void unregisterPopoutArea(String ID) { popoutAreas.remove(ID); }
		public static BooleanProperty showPopup(Pane content, String popoutID)
		{
			if(!popoutAreas.containsKey(popoutID))
			{
				Log.getLogger().warn("No popout area has been registered with ID " + popoutID + " yet!");
				return null;
			}
			
			PopoutPane pop = new PopoutPane(content, popoutAreas.get(popoutID), null);
			popoutAreas.get(popoutID).getChildren().add(pop);
			return pop.getCloseOnTrue();
		}
	}
}
