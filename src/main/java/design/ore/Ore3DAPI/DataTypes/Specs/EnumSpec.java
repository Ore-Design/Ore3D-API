package design.ore.Ore3DAPI.DataTypes.Specs;

import java.util.concurrent.Callable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import design.ore.Ore3DAPI.DataTypes.Interfaces.ISpecUI;
import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import design.ore.Ore3DAPI.DataTypes.Specs.UI.EnumSpecUI;
import design.ore.Ore3DAPI.Util.Mapper;
import lombok.Getter;

public class EnumSpec<E extends Enum<E>> extends Spec<E>
{
	public EnumSpec(Build parent, String id, E initialValue, boolean readOnly, String section, boolean countsAsMatch)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, null); }
	
	public EnumSpec(Build parent, String id, E initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<E> calculateOnDirty)
	{ this(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, null); }

	public EnumSpec(Build parent, String id, E initialValue, boolean readOnly, String section, boolean countsAsMatch, Callable<E> calculateOnDirty, String uniqueBehaviorNotifier)
	{
		super(parent, id, initialValue, readOnly, section, countsAsMatch, calculateOnDirty, uniqueBehaviorNotifier);

		this.clazz = initialValue.getDeclaringClass();
		Mapper.getMapper().registerSubtypes(initialValue.getDeclaringClass());
	}
	
	@Override
	@JsonProperty("val")
	// We must serialize type by Name or serialization fails (for some reason)
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	public E getValue() { return super.getValue(); }

	@Override
	@JsonProperty("val")
	// We must serialize type by Name or serialization fails (for some reason)
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	public void setValue(E val) { super.setValue(val); }

	@Getter @JsonIgnore final Class<E> clazz;

	@Override
	public ISpecUI<E> generateUI() { return new EnumSpecUI<E>(this); }
}
