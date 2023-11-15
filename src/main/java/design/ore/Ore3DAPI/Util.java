package design.ore.Ore3DAPI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextFormatter;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import lombok.Getter;

public class Util
{
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
	
	@Getter private static final List<ClassLoader> registeredClassLoaders = new ArrayList<ClassLoader>();
	public static void registerClassLoader(ClassLoader cl) { registeredClassLoaders.add(cl); }
	
	@Getter private static final Map<String, Map<Integer, String>> registeredIntegerStringMaps = new HashMap<>();
	public static void registerIntStringMap(String mapID, Map<Integer, String> map)
	{
		if(registeredIntegerStringMaps.containsKey(mapID)) throw new IllegalArgumentException("A map with the ID " + mapID + " has already been registered!");
		else registeredIntegerStringMaps.put(mapID, map);
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
	{
	    List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
	    list.sort(Entry.comparingByValue());
	
	    Map<K, V> result = new LinkedHashMap<>();
	    for (Entry<K, V> entry : list) { result.put(entry.getKey(), entry.getValue()); }
	
	    return result;
	}

	public static String stackTraceArrayToString(Exception e)
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
	
	public static class UI
	{
		public static void colorize(ImageView img, Color color)
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
		}
		
		public static void checkboxMatchSize(CheckBox box)
		{
			box.heightProperty().addListener(l -> ((Region) box.lookup(".mark")).setPadding(new Insets((box.getHeight()/2) - 7)));
		}
	}
}
