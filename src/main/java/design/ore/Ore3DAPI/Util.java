package design.ore.Ore3DAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpRequestBase;
import org.controlsfx.control.Notifications;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMPricing;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingPricing;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.DataTypes.Protected.Transaction;
import design.ore.Ore3DAPI.UI.PopoutStage;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.Getter;
import lombok.NonNull;
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
	
	public class Auth
	{
		private static Function<HttpRequestBase, HttpRequestBase> registeredAuthSigner;
		public static void registerAuthSigner(Function<HttpRequestBase, HttpRequestBase> authSigner)
		{
			if(registeredAuthSigner != null)
			{
				Log.getLogger().warn("A second auth signer was attempted to be registered! Ignoring...");
				return;
			}
			registeredAuthSigner = authSigner;
		}
		public static HttpRequestBase signWithcORECreds(HttpRequestBase request)
		{
			if(registeredAuthSigner == null)
			{
				Log.getLogger().warn("Auth Signer is not yet registered! Cannot sign!");
				return request;
			}
			return registeredAuthSigner.apply(request);
		}
	}
	
	public class Log
	{
		@Getter protected static Logger logger = null;
		public static Appender<ILoggingEvent> getAppender(String name) { if(logger != null) { return logger.getAppender(name); } else return null; }
	}
	
	public class Mapper
	{
		@Getter protected static ObjectMapper mapper = null;
		protected static Callable<ObjectMapper> mapperFactory = null;
		public static ObjectMapper createMapper()
		{
			if(mapperFactory != null)
			{
				try { return mapperFactory.call(); }
				catch (Exception e) { Log.getLogger().warn(Util.formatThrowable("Error creating new mapper!", e)); }
			}
			else Log.getLogger().warn("Mapper Creator has not yet been registered!");
			
			return null;
		}
		public static <T> T quickClone(@NonNull T toClone, Class<? extends T> clazz)
		{
			try
			{
				String serialized = mapper.writeValueAsString(toClone);
//				Log.getLogger().debug("Running quick clone, JSON is\n" + serialized);
				return mapper.readValue(serialized, clazz);
			}
			catch (Exception e)
			{
				Log.getLogger().warn(Util.formatThrowable("Error quick cloning object " + toClone, e));
				return null;
			}
		}
		public static <T> T quickClone(@NonNull T toClone, TypeReference<? extends T> type)
		{
			try { return mapper.readValue(mapper.writeValueAsString(toClone), type); }
			catch (Exception e)
			{
				Log.getLogger().warn(Util.formatThrowable("Error quick cloning object " + toClone, e));
				return null;
			}
		}
		
		public static void printItem(String name, Object obj)
		{
			try { Log.getLogger().info(name + ": " + mapper.writeValueAsString(obj)); }
			catch(Exception e) { Log.getLogger().warn(Util.formatThrowable("Failed to print item " + name + "!", e)); }
		}
	}
	
	@Getter private static Image brokenChainIcon;
	@Getter private static Image chainIcon;
	@Getter private static Image xIcon;
	static
	{
		try
		{
			brokenChainIcon = new Image("ui/icons/BrokenChainIcon.png");
			chainIcon = new Image("ui/icons/ChainIcon.png");
			xIcon = new Image("ui/icons/XIcon.png");
		}
		catch(Exception e) { Log.getLogger().warn(Util.formatThrowable("Failed to initialize UI elements!", e)); }
	}
	
	public static DoubleBinding zeroDoubleBinding() { return Bindings.createDoubleBinding(() -> 0.0); }
	public static StringExpression stringExpression(String str) { return Bindings.createStringBinding(() -> str); } 
	public static BooleanBinding booleanBinding(boolean bool) { return Bindings.createBooleanBinding(() -> bool); }
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
	{
	    List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
	    list.sort(Entry.comparingByValue());
	
	    Map<K, V> result = new LinkedHashMap<>();
	    for (Entry<K, V> entry : list) { result.put(entry.getKey(), entry.getValue()); }
	
	    return result;
	}

	public static String stackTraceArrayToString(Throwable e)
	{
		return stackTraceArrayToString(e.getStackTrace());
	}

	public static String stackTraceArrayToString(StackTraceElement[] e)
	{
		StringBuilder str = new StringBuilder();
		for(StackTraceElement el : e) { str.append(el.toString() + "\n"); }
		return str.toString();
	}
	
	public static String formatThrowable(String userDefinedMessage, Throwable e) { return userDefinedMessage + " - " + e.getMessage() + "\n" + stackTraceArrayToString(e); }
	
	public static TextFormatter<?> getDecimalFormatter(int decimalPlaces)
	{
		return new TextFormatter<>((UnaryOperator<TextFormatter.Change>) change ->
		{ return Pattern.compile("(\\+|-)?(\\d*|\\d+\\.\\d{0," + decimalPlaces + "})").matcher(change.getControlNewText()).matches() ? change : null; });
	}
	
	public static TextFormatter<?> getIntegerFormatter()
	{
		return new TextFormatter<>((UnaryOperator<TextFormatter.Change>) change ->
		{ return Pattern.compile("(\\+|-)?(\\d*)").matcher(change.getControlNewText()).matches() ? change : null; });
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
		if(pricing.isPresent()) return entry.duplicate(pricing.get().getCostPerUnit(), 1, parent.getQuantity(), isCustom);
		else return entry.duplicate(1, parent.getQuantity(), isCustom);
	}
	
	public static BOMEntry duplicateBOMWithPricing(Transaction transaction, Build parent, BOMEntry entry, BOMEntry originalEntry)
	{
		Optional<BOMPricing> pricing = transaction.getPricing().getBom().stream().filter(bp -> bp.getInternalID().equals(entry.getId())).findFirst();
		BOMEntry newEntry = null;
		if(pricing.isPresent()) newEntry = entry.duplicate(pricing.get().getCostPerUnit(), originalEntry.getUnoverriddenQuantityProperty().get(),
			parent.getQuantity(), originalEntry.getCustomEntryProperty().get(), originalEntry.getIgnoreParentQuantityProperty().get());
		else newEntry = entry.duplicate(originalEntry.getUnoverriddenQuantityProperty().get(), parent.getQuantity(),
			originalEntry.getCustomEntryProperty().get(), originalEntry.getIgnoreParentQuantityProperty().get());
		
		if(originalEntry.getQuantityOverriddenProperty().get()) newEntry.getOverridenQuantityProperty().set(originalEntry.getOverridenQuantityProperty().get());
		if(originalEntry.getMarginOverriddenProperty().get()) newEntry.getOverridenMarginProperty().set(originalEntry.getOverridenMarginProperty().get());
		
		return newEntry;
	}
	
	public static RoutingEntry duplicateRoutingWithPricing(Transaction transaction, Build parent, RoutingEntry entry, boolean isCustom, Double overriddenQuantity)
	{
		Optional<RoutingPricing> pricing = transaction.getPricing().getRoutings().stream().filter(bp -> bp.getId().equals(entry.getId())).findFirst();
		if(pricing.isPresent()) return entry.duplicate(pricing.get().getCostPerMinute(), 1d, parent.getQuantity(),
			isCustom, entry.getQuantityOverriddenProperty().get() ? entry.getOverridenQuantityProperty().get() : null);
		else return entry.duplicate(null, 1d, parent.getQuantity(), isCustom, entry.getQuantityOverriddenProperty().get() ? entry.getOverridenQuantityProperty().get() : null);
	}
	
	public static RoutingEntry duplicateRoutingWithPricing(Transaction transaction, Build parent, RoutingEntry entry, RoutingEntry originalEntry)
	{
		Optional<BOMPricing> pricing = transaction.getPricing().getBom().stream().filter(bp -> bp.getInternalID().equals(entry.getId())).findFirst();
		RoutingEntry newEntry = null;
		if(pricing.isPresent()) newEntry = entry.duplicate(pricing.get().getCostPerUnit(), originalEntry.getUnoverriddenQuantityProperty().get(),
			parent.getQuantity(), originalEntry.getCustomEntryProperty().get(), null);
		else newEntry = entry.duplicate(null, originalEntry.getUnoverriddenQuantityProperty().get(), parent.getQuantity(),
			originalEntry.getCustomEntryProperty().get(), null);
		
		if(originalEntry.getQuantityOverriddenProperty().get()) newEntry.getOverridenQuantityProperty().set(originalEntry.getOverridenQuantityProperty().get());
		
		return newEntry;
	}
	
	public static class TransactionLoading
	{
		private static final Map<String, Consumer<Transaction>> registeredTransactionLoaders = new HashMap<>();
		public static void registerOrderLoader(String id, Consumer<Transaction> loader)
		{
			if(registeredTransactionLoaders.containsKey(id)) Log.getLogger().warn("There is already a registered transaction loader with ID " + id + "! Overriding...");
			registeredTransactionLoaders.put(id, loader);
		}
		public static void loadTransaction(String id, Transaction transactionToLoad)
		{
			Consumer<Transaction> loader = registeredTransactionLoaders.get(id);
			if(loader != null)
			{
				try { loader.accept(transactionToLoad); }
				catch(Exception e) { Log.getLogger().warn("Unable to load transaction with loader ID " + id + " because " + e.getMessage() + "\n" + Util.stackTraceArrayToString(e));}
			}
			else Log.getLogger().warn("Unable to load transaction, as there is no transaction loader registered with the ID " + id + "!");
		}
	}
	
	public static class UI
	{
		public static <T> T runOnApplicationThread(Callable<T> call)
		{
			if(Platform.isFxApplicationThread())
			{
				try { return call.call(); }
				catch (Exception e)
				{
					Log.getLogger().warn("Error running callable: " + e.getMessage() + "\n" + Util.stackTraceArrayToString(e));
					return null;
				}
			}
			else
			{
				final FutureTask<T> task = new FutureTask<>(new Callable<>()
				{
					@Override
					public T call() throws Exception { return call.call(); }
				});
				Platform.runLater(task);
				
				try { return task.get(); }
				catch (InterruptedException | ExecutionException e)
				{
					Log.getLogger().warn("Error running alert! " + e.getMessage() + "\n" + Util.stackTraceArrayToString(e));
					return null;
				}
			}
		}
		
		public static void notify(String title, String message, double seconds)
		{
			Optional<Window> owner = Stage.getWindows().stream().filter(s -> s.isFocused() && s instanceof Stage).findFirst();
			if(owner.isEmpty())
			{
				Log.getLogger().warn("Cant show notification, as there is no currently focused window!");
				return;
			}
			
			if(Platform.isFxApplicationThread())
			{
				try { showNotification((Stage) owner.get(), title, message, seconds); }
				catch (Exception e) { Log.getLogger().warn("Error running notification: " + e.getMessage() + "\n" + Util.stackTraceArrayToString(e)); }
			}
			else
			{
				final FutureTask<Void> task = new FutureTask<>(new Callable<>()
				{
					@Override
					public Void call() throws Exception { showNotification((Stage) owner.get(), title, message, seconds); return null; }
				});
				Platform.runLater(task);
			}
		}
		
		private static void showNotification(Stage owner, String title, String message, double seconds)
		{
			Notifications thresholdNotif = Notifications.create().title("Multiple Changes").text("Multiple changes affected!").hideAfter(Duration.seconds(5))
				.owner(owner).position(Pos.TOP_RIGHT).styleClass("notification");
			
			Notifications.create().threshold(3, thresholdNotif).title(title).text(message).hideAfter(Duration.seconds(seconds))
				.owner(owner).position(Pos.TOP_RIGHT).styleClass("notification").show();
		}
		
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
		
		public static Alert confirm(String title, String message)
		{
			Alert confirm = new Alert(AlertType.CONFIRMATION);
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
		
		private static final Map<String, Stage> popoutAreas = new HashMap<>();
		public static void registerPopoutArea(String ID, Stage stage)
		{
			if(popoutAreas.containsKey(ID)) throw new IllegalArgumentException("Popout area with ID " + ID + " has already been registered!");
			else popoutAreas.put(ID, stage);
		}
		public static void unregisterPopoutArea(String ID) { popoutAreas.remove(ID); }
		public static BooleanProperty showPopup(Pane content, String navigationID, String title, boolean useStylesheet)
		{
			if(!popoutAreas.containsKey(navigationID))
			{
				Log.getLogger().warn("No popout area has been registered with ID " + navigationID + " yet!");
				return null;
			}
			else if(Stage.getWindows().stream().anyMatch(s -> s instanceof Stage && s.isShowing() &&
				((Stage) s).getTitle().equals(title) && ((Stage) s).getOwner().equals(popoutAreas.get(navigationID))))
			{
				Log.getLogger().debug("A stage already exists with title " + title + " in registered ID " + navigationID + "!");
				return null;
			}
			
			PopoutStage<Void> stage = new PopoutStage<>(popoutAreas.get(navigationID), content, title, useStylesheet);
			stage.show();
			
			return stage.getCloseOnTrue();
		}
		
		public static void showPopupAndWait(Pane content, String navigationID, String title, boolean useStylesheet)
		{
			if(!popoutAreas.containsKey(navigationID))
			{
				Log.getLogger().warn("No popout area has been registered with ID " + navigationID + " yet!");
				return;
			}
			else if(Stage.getWindows().stream().anyMatch(s -> s instanceof Stage && s.isShowing() &&
				((Stage) s).getTitle().equals(title) && ((Stage) s).getOwner().equals(popoutAreas.get(navigationID))))
			{
				Log.getLogger().debug("A stage already exists with title " + title + " in registered ID " + navigationID + "!");
				return;
			}
			
			PopoutStage<Void> stage = new PopoutStage<Void>(popoutAreas.get(navigationID), content, title, useStylesheet);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.showAndWait();
		}
		
		public static void closePopouts(String popoutID)
		{
			List<Stage> toClose = new ArrayList<>();
			for(Window w : Stage.getWindows())
			{
				if(w instanceof Stage && ((Stage) w).getOwner() != null && ((Stage) w).getOwner().equals(popoutAreas.get(popoutID))) toClose.add((Stage) w);
			}
			
			for(Stage s : toClose) { s.close(); }
		}
	}
	
	@Getter private static String persistentPluginDataDir;
	public static void setPersistentPluginDataDir(String dir)
	{
		if(persistentPluginDataDir == null) persistentPluginDataDir = dir;
		else Log.getLogger().warn("Cannot set Appdata Dir as it is already set!");
	}
}
