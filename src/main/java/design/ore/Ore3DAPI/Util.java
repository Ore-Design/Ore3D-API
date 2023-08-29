package design.ore.Ore3DAPI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class Util
{
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
	
	public static class UI
	{
		public static void colorize(ImageView img, Color color)
		{
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
