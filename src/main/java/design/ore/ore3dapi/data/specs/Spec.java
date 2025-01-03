package design.ore.ore3dapi.data.specs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import design.ore.ore3dapi.Util;
import design.ore.ore3dapi.data.core.Build;
import design.ore.ore3dapi.data.interfaces.ISpecUI;
import design.ore.ore3dapi.data.interfaces.ISummaryOption;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(value = {"bound", "name", "bean"}, ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class Spec<T> extends SimpleObjectProperty<T> implements ISummaryOption
{
	@Getter @JsonIgnore protected final SimpleBooleanProperty readOnlyProperty = new SimpleBooleanProperty(false);
	@JsonIgnore public boolean isReadOnly() { return readOnlyProperty.get(); }
	@JsonIgnore public void setReadOnly(boolean readOnly) { readOnlyProperty.set(readOnly); }
	
	@Getter @JsonIgnore protected final SimpleBooleanProperty countsAsMatchProperty = new SimpleBooleanProperty(false);
	@JsonIgnore public boolean countsAsMatch() { return countsAsMatchProperty.get(); }
	@JsonIgnore public void setCountsAsMatch(boolean countsAsMatch) { countsAsMatchProperty.set(countsAsMatch); }
	
	public Spec() { this(null); }
	
	public Spec(Build parent) { parentBuild = parent; }
	
	public Spec(Build parent, String id, T initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<T> calculateOnDirty, String uniqueBehaviorNotifier)
	{
		this.id = id;
		setValue(initialValue);
		this.countsAsMatchProperty.set(countsAsMatch);
		this.readOnlyProperty.set(readOnly);
		this.section = section;
		this.calculateOnDirty = calculateOnDirty;
		this.parentBuild = parent;
		this.uniqueBehaviorNotifierProperty.setValue(uniqueBehaviorNotifier);
		
		addListener((obs, oldVal, newVal) -> linkedSpecs.forEach(s -> { if(s.linkIsActive()) s.setValue(newVal); }));
		previousUIProperty.addListener((obs, oldVal, newVal) ->
		{
			if(oldVal != null)
			{
				Parent uiParent = oldVal.getUINode().getParent();
				if(uiParent instanceof VBox) ((VBox) uiParent).getChildren().remove(oldVal.getUINode());
				oldVal.unbindUI();
			}
		});
		
		linkedSpecs.addListener((Change<? extends Spec<T>> c) ->
		{
			while(c.next())
			{
				c.getAddedSubList().forEach(rem -> linkIsActiveProperty.bindBidirectional(rem.getLinkIsActiveProperty()));
				c.getRemoved().forEach(rem -> linkIsActiveProperty.unbindBidirectional(rem.getLinkIsActiveProperty()));
			}
		});
	}
	
	// These overrides allow us to change the JSON key for these values
	@Override
	@JsonProperty("val")
	public T getValue() { return super.getValue(); }

	@Override
	@JsonProperty("val")
	public void setValue(T val) { super.setValue(val); }
	
	// TODO: Link spec UI for all specs other than DoubleSpec
	protected final ObservableList<Spec<T>> linkedSpecs = FXCollections.observableArrayList();
	public void link(Spec<T> toLink)
	{
		linkedSpecs.add(toLink);
		toLink.linkFromOtherSpec(this);
	}
	protected void linkFromOtherSpec(Spec<T> toLink) { linkedSpecs.add(toLink); }
	@Getter @JsonIgnore private final SimpleBooleanProperty linkIsActiveProperty = new SimpleBooleanProperty(false);
	@JsonIgnore public boolean linkIsActive() { return linkIsActiveProperty.get(); }
	@JsonIgnore public void setLinkIsActive(boolean link) { linkIsActiveProperty.set(link); }
	public BooleanBinding isLinked() { return Bindings.createBooleanBinding(() -> linkedSpecs.size() > 0, linkedSpecs); }
	
	// TODO: Figure out how/if these can be final
	@JsonIgnore @Getter @Setter protected String section;
	@JsonIgnore @Getter @Setter protected String id;
	
	@JsonIgnore @Getter @Setter protected Callable<T> calculateOnDirty;
	@JsonIgnore @Getter protected final Build parentBuild;
	
	@JsonIgnore @Getter protected final SimpleStringProperty uniqueBehaviorNotifierProperty = new SimpleStringProperty();
	@JsonIgnore @Getter protected final SimpleBooleanProperty holdCalculateTillCompleteProperty = new SimpleBooleanProperty(false);
	
	@JsonIgnore public void setPropertyToCallable()
	{
		if(calculateOnDirty != null)
		{
			try
			{
				T calledValue = calculateOnDirty.call();
				if(calledValue != null) setValue(calledValue);
			}
			catch (Exception e) { Util.Log.getLogger().warn(Util.formatThrowable("Error assigning value from Callable to property!", e)); }
		}
	}
	
	public abstract ISpecUI<T> generateUI();
	private SimpleObjectProperty<ISpecUI<T>> previousUIProperty = new SimpleObjectProperty<>();
	public void clearPreviousUI() { previousUIProperty.set(null); }
	
	@SuppressWarnings("unchecked") // We check the spec conversion in the for loop/cleanedSpecs check
	@JsonIgnore public final Node getUI(List<Spec<?>> props, String popoutID)
	{
		ISpecUI<T> ui = generateUI();

		if(props == null || props.size() == 0) { ui.rebind(null, popoutID); }
		else
		{
			// Filter out specs of differing types
			List<Spec<T>> cleanedSpecs = new ArrayList<>();
			for(Spec<?> s : props) { if(s.getClass().equals(this.getClass())) cleanedSpecs.add((Spec<T>) s); }
			ui.rebind((List<Spec<T>>) cleanedSpecs, popoutID);
		}
		
		previousUIProperty.set(ui);
		return ui.getUINode();
	}
	
	public boolean matches(Spec<?> spec)
	{
		if(spec == this) return true;
		
		return this.id.equals(spec.id) && this.getValue().equals(spec.getValue());
	}
	
	@Override
	public final boolean equals(Object o)
	{
		if(o == this) return true;
		
		if(!(o instanceof Spec)) return false;
		
		Spec<?> compare = (Spec<?>) o;
		return this.id.equals(compare.id);
	}
	
	@Override public String getSearchName() { return "Spec - " + id; }
	@Override public Object getSummaryValue() { return this; }
	
	@JsonIgnore @Getter private final BooleanProperty selectedProperty = new SimpleBooleanProperty(false);
}
