package design.ore.api.ore3d;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import design.ore.api.ore3d.data.core.Build;
import design.ore.api.ore3d.data.core.Transaction;
import design.ore.api.ore3d.data.pricing.BOMEntry;
import design.ore.api.ore3d.data.pricing.BOMPricing;
import design.ore.api.ore3d.data.pricing.RoutingEntry;
import design.ore.api.ore3d.data.pricing.RoutingPricing;
import design.ore.api.ore3d.ui.PopoutStage;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.*;
import javafx.css.converter.ColorConverter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import org.apache.http.client.methods.HttpRequestBase;
import org.controlsfx.control.Notifications;
import org.slf4j.LoggerFactory;

import java.security.Provider;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class Util
{

	public static class Log
	{
		@Getter private static final Logger logger;
		static { logger = (Logger) LoggerFactory.getLogger(Util.class); }
	}

	public static class Colors
	{
		@Getter private final static SimpleObjectProperty<Color> foregroundProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> secondaryForegroundProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> tertiaryForegroundProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> transparentForegroundProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> secondaryTransparentForegroundProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> tertiaryTransparentForegroundProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> backgroundProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> secondaryBackgroundProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> transparentBackgroundProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> dimBackgroundProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> accentProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> errorProperty = new SimpleObjectProperty<>();
		@Getter private final static SimpleObjectProperty<Color> warningProperty = new SimpleObjectProperty<>();
	}
	
	public static class Auth
	{
		private static Function<HttpRequestBase, HttpRequestBase> registeredAuthSigner;
		public static void registerAuthSigner(Function<HttpRequestBase, HttpRequestBase> authSigner)
		{
			if(registeredAuthSigner != null)
			{
				Log.logger.warn("A second auth signer was attempted to be registered! Ignoring...");
				return;
			}
			registeredAuthSigner = authSigner;
		}
		public static HttpRequestBase signWithcORECreds(HttpRequestBase request)
		{
			if(registeredAuthSigner == null)
			{
				Log.logger.warn("Auth Signer is not yet registered! Cannot sign!");
				return request;
			}
			return registeredAuthSigner.apply(request);
		}
	}
	
	public static class Mapper
	{
		@Getter protected static ObjectMapper mapper = null;
		protected static Callable<ObjectMapper> mapperFactory = null;

		public static ObjectMapper createMapper()
		{
			if(mapperFactory != null)
			{
				try { return mapperFactory.call(); }
				catch (Exception e) { Log.logger.warn("Error creating new mapper!", e); }
			}
			else Log.logger.warn("Mapper Creator has not yet been registered!");
			
			return null;
		}

		public static <T> T quickClone(@NonNull T toClone, Class<? extends T> clazz)
		{
			try
			{
				String serialized = mapper.writeValueAsString(toClone);
//				Log.logger.debug("Running quick clone, JSON is\n" + serialized);
				return mapper.readValue(serialized, clazz);
			}
			catch (Exception e)
			{
                Log.logger.warn("Error quick cloning object {}", toClone, e);
				return null;
			}
		}

		public static <T> T quickClone(@NonNull T toClone, TypeReference<? extends T> type)
		{
			try { return mapper.readValue(mapper.writeValueAsString(toClone), type); }
			catch (Exception e)
			{
                Log.logger.warn("Error quick cloning object {}", toClone, e);
				return null;
			}
		}
		
		public static void printItem(String name, Object obj)
		{
			try {
                Log.logger.info("{}: {}", name, mapper.writeValueAsString(obj)); }
			catch(Exception e) { Log.logger.warn("Failed to print item {}!", name, e); }
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
		catch(Exception e) { Log.logger.warn("Failed to initialize UI elements!", e); }
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
		if(pricing.isPresent()) return entry.duplicate(pricing.get().getCostPerUnit(), 1, parent, isCustom);
		else return entry.duplicate(1, parent, isCustom);
	}
	
	public static BOMEntry duplicateBOMWithPricing(Transaction transaction, Build parent, BOMEntry entry, BOMEntry originalEntry)
	{
		Optional<BOMPricing> pricing = transaction.getPricing().getBom().stream().filter(bp -> bp.getInternalID().equals(entry.getId())).findFirst();
		BOMEntry newEntry = null;
		if(pricing.isPresent())
		{
			Integer margin = pricing.get().getMargin();
			newEntry = entry.duplicate(pricing.get().getCostPerUnit(), originalEntry.getUnoverriddenQuantityProperty().get(),
				parent, originalEntry.getCustomEntryProperty().get(), originalEntry.getIgnoreParentQuantityProperty().get(), margin == null || margin < 0 ? originalEntry.getUnoverriddenMargin() : margin);
		}
		else newEntry = entry.duplicate(originalEntry.getUnoverriddenQuantityProperty().get(), parent,
			originalEntry.getCustomEntryProperty().get(), originalEntry.getIgnoreParentQuantityProperty().get());
		
		if(originalEntry.getQuantityOverriddenProperty().get()) newEntry.getOverridenQuantityProperty().set(originalEntry.getOverridenQuantityProperty().get());
		if(originalEntry.getMarginOverriddenProperty().get()) newEntry.getOverridenMarginProperty().set(originalEntry.getOverridenMarginProperty().get());
		
		newEntry.putStoredValues(originalEntry.getStoredValues());
		
		return newEntry;
	}
	
	public static RoutingEntry duplicateRoutingWithPricing(Transaction transaction, Build parent, RoutingEntry entry, boolean isCustom, Double overriddenQuantity)
	{
		Optional<RoutingPricing> pricing = transaction.getPricing().getRoutings().stream().filter(bp -> bp.getId().equals(entry.getId())).findFirst();
		if(pricing.isPresent()) return entry.duplicate(pricing.get().getCostPerMinute(), 1d, parent,
			entry.getQuantityOverriddenProperty().get() ? entry.getOverridenQuantityProperty().get() : null, isCustom);
		else return entry.duplicate(1d, parent, entry.getQuantityOverriddenProperty().get() ? entry.getOverridenQuantityProperty().get() : null, isCustom);
	}
	
	public static RoutingEntry duplicateRoutingWithPricing(Transaction transaction, Build parent, RoutingEntry entry, RoutingEntry originalEntry)
	{
		Optional<RoutingPricing> pricing = transaction.getPricing().getRoutings().stream().filter(rp -> rp.getId().equals(entry.getId())).findFirst();
		RoutingEntry newEntry = null;
		if(pricing.isPresent())
		{
			newEntry = entry.duplicate(pricing.get().getCostPerMinute(), originalEntry.getUnoverriddenQuantityProperty().get(), parent, originalEntry.getCustomEntryProperty().get());
			newEntry.setMargin(pricing.get().getMargin());
		}
		else
		{
			newEntry = entry.duplicate(originalEntry.getUnoverriddenQuantityProperty().get(), parent, originalEntry.getCustomEntryProperty().get());
			newEntry.setMargin(originalEntry.getMargin());
		}
		
		if(originalEntry.getQuantityOverriddenProperty().get()) newEntry.getOverridenQuantityProperty().set(originalEntry.getOverridenQuantityProperty().get());
		
		return newEntry;
	}
	
	public static class TransactionLoading
	{
		private static final Map<String, Consumer<Transaction>> registeredTransactionLoaders = new HashMap<>();
		public static void registerOrderLoader(String id, Consumer<Transaction> loader)
		{
			if(registeredTransactionLoaders.containsKey(id)) Log.logger.warn("There is already a registered transaction loader with ID " + id + "! Overriding...");
			registeredTransactionLoaders.put(id, loader);
		}
		public static void loadTransaction(String id, Transaction transactionToLoad)
		{
			Consumer<Transaction> loader = registeredTransactionLoaders.get(id);
			if(loader != null)
			{
				try { loader.accept(transactionToLoad); }
				catch(Exception e) { Log.logger.warn("Unable to load transaction with loader ID {}", id, e);}
			}
			else
                Log.logger.warn("Unable to load transaction, as there is no transaction loader registered with the ID {}!", id);
		}
	}
	
	public static class Styling
	{
		public static final String DARK_STYLESHEET = "stylesheets/dark.css";
		public static final String BOW_STYLESHEET = "stylesheets/bow.css";
		public static final String REYMALA_STYLESHEET = "stylesheets/reymala.css";
		
		@Getter private static final ObservableList<String> styleOptions = FXCollections.observableArrayList(DARK_STYLESHEET, BOW_STYLESHEET, REYMALA_STYLESHEET);
		@Getter private static final Map<String, String> styleDisplayNames = Map.of("Dark", DARK_STYLESHEET, "Black & White", BOW_STYLESHEET, "Reymala", REYMALA_STYLESHEET);
		
		@Getter private static final SimpleStringProperty stlysheetProperty;
		static {
			stlysheetProperty = new SimpleStringProperty();
			stlysheetProperty.addListener((obs, oldVal, newVal) -> getColors(newVal));
			stlysheetProperty.setValue(DARK_STYLESHEET);
		}
		
		public static void bindUIToStylesheet(Object ui)
		{
			if(ui instanceof Scene) ((Scene) ui).getStylesheets().add(stlysheetProperty.getValue());
			else if(ui instanceof Parent) ((Parent) ui).getStylesheets().add(stlysheetProperty.getValue());
			
			stlysheetProperty.addListener((obs, oldVal, newVal) ->
			{
				if(ui instanceof Scene)
				{
					((Scene) ui).getStylesheets().remove(oldVal);
					((Scene) ui).getStylesheets().add(newVal);
				}
				else if(ui instanceof Parent)
				{
					((Parent) ui).getStylesheets().remove(oldVal);
					((Parent) ui).getStylesheets().add(newVal);
				}
			});
		}
	    
		@SuppressWarnings("unchecked")
		private static void getColors(String stylesheet)
	    {
	    	Util.Log.logger.info("Retrieving colors from stylesheet...");
	    	
			CssParser parser = new CssParser();
			StyleConverter<String, Color> converter = ColorConverter.getInstance();
			try
			{
				Stylesheet css = parser.parse(Styling.class.getClassLoader().getResource(stylesheet).toURI().toURL());
				final Rule root = css.getRules().getFirst();
				for(Declaration d : root.getDeclarations())
				{
					switch(d.getProperty())
					{
						case "-foreground":
							Colors.getForegroundProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-foreground-2":
							Colors.getSecondaryForegroundProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-foreground-3":
							Colors.getTertiaryForegroundProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-foreground-transparent":
							Colors.getTransparentForegroundProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-foreground-transparent-2":
							Colors.getSecondaryTransparentForegroundProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-foreground-transparent-3":
							Colors.getTertiaryTransparentForegroundProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-background":
							Colors.getBackgroundProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-background-2":
							Colors.getSecondaryBackgroundProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-background-transparent":
							Colors.getTransparentBackgroundProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-background-dim":
							Colors.getDimBackgroundProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-accent":
							Colors.getAccentProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-warning-conflict":
							Colors.getWarningProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						case "-error-conflict":
							Colors.getErrorProperty().setValue(converter.convert(d.getParsedValue(), null));
							break;
						default:
							Util.Log.logger.warn("Unknown color found: " + d.getProperty());
					}
				}
			}
			catch(Exception e) { Util.Log.logger.warn(e.toString()); }
			
	    	Util.Log.logger.info("Colors retrieved!");
	    }
	}
	
	public static class UI
	{
		private static Supplier<String> currentMenuIdSupplier = null;
		public static void setCurrentMenuIdSupplier(Supplier<String> supplier)
		{
			if(currentMenuIdSupplier == null) currentMenuIdSupplier = supplier;
		}
		public static String getCurrentMenuId()
		{
			if(currentMenuIdSupplier == null) return "";

			return currentMenuIdSupplier.get();
		}

		public static <T> T runOnApplicationThread(Callable<T> call)
		{
			if(Platform.isFxApplicationThread())
			{
				try { return call.call(); }
				catch (Exception e)
				{
					Log.logger.warn("Error running callable!", e);
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
					Log.logger.warn("Error running alert!", e);
					return null;
				}
			}
		}
		
		public static void notify(String title, String message, double seconds)
		{
			Optional<Window> owner = Stage.getWindows().stream().filter(s -> s.isFocused() && s instanceof Stage).findFirst();
			if(owner.isEmpty())
			{
				Log.logger.warn("Cant show notification, as there is no currently focused window!");
				return;
			}
			
			if(Platform.isFxApplicationThread())
			{
				try { showNotification((Stage) owner.get(), title, message, seconds); }
				catch (Exception e) { Log.logger.warn("Error running notification!", e); }
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
	        Styling.bindUIToStylesheet(confirm.getDialogPane());
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
	        Styling.bindUIToStylesheet(confirm.getDialogPane());
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
	        Styling.bindUIToStylesheet(info.getDialogPane());
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
	        Styling.bindUIToStylesheet(warn.getDialogPane());
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
	        Styling.bindUIToStylesheet(error.getDialogPane());
			error.initStyle(StageStyle.UNDECORATED);
			error.setGraphic(null);
			
			return error;
		}
		
		public static ImageView colorize(ImageView img, ObjectProperty<Color> color)
		{

			ImageView checkClip = new ImageView(img.getImage());
			img.setClip(checkClip);
			img.setPreserveRatio(true);
			checkClip.setPreserveRatio(true);
			checkClip.fitWidthProperty().bind(img.fitWidthProperty());

			if(img.getImage() == null) return img;

	        img.effectProperty().bind(Bindings.createObjectBinding(() ->
	        {
	        	ColorAdjust monochrome = new ColorAdjust();
	        	monochrome.setSaturation(-1.0);
	        	return new Blend(BlendMode.MULTIPLY, monochrome, new ColorInput( 0, 0, img.getImage().getWidth(), img.getImage().getHeight(), color.getValue()));
	        }, color));
	        
	        return img;
		}
		
		public static void checkboxMatchSize(CheckBox box)
		{
			box.heightProperty().addListener(l -> ((Region) box.lookup(".mark")).setPadding(new Insets((box.getHeight() * 0.35))));
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
				Log.logger.warn("No popout area has been registered with ID " + navigationID + " yet!");
				return null;
			}
			else if(Stage.getWindows().stream().anyMatch(s -> s instanceof Stage && s.isShowing() &&
				((Stage) s).getTitle().equals(title) && ((Stage) s).getOwner().equals(popoutAreas.get(navigationID))))
			{
				Log.logger.debug("A stage already exists with title " + title + " in registered ID " + navigationID + "!");
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
				Log.logger.warn("No popout area has been registered with ID " + navigationID + " yet!");
				return;
			}
			else if(Stage.getWindows().stream().anyMatch(s -> s instanceof Stage && s.isShowing() &&
				((Stage) s).getTitle().equals(title) && ((Stage) s).getOwner().equals(popoutAreas.get(navigationID))))
			{
				Log.logger.debug("A stage already exists with title " + title + " in registered ID " + navigationID + "!");
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
		else Log.logger.warn("Cannot set Appdata Dir as it is already set!");
	}
}
