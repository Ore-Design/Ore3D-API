package design.ore.Ore3DAPI.Jackson;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import design.ore.Ore3DAPI.DataTypes.Build.Tag;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class ObservableSetSerialization
{
	public static class TagSet
	{
		public static class Serializer extends StdSerializer<ObservableSet<Tag>>
		{
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableSet<Tag>> t) { super(t); }
		
			@Override
			public void serialize(ObservableSet<Tag> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeObject(new ArrayList<Tag>(value));
			}
			
			@Override
			public void serializeWithType(ObservableSet<Tag> value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("tagobslist");
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<ObservableSet<Tag>>
		{
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableSet<Tag>> t) { super(t); }
	
			@Override
			public ObservableSet<Tag> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<Tag> list = p.readValueAs(new TypeReference<ArrayList<Tag>>() {});
				ObservableSet<Tag> set = FXCollections.observableSet();
				for(Tag t : list) { set.add(t); }
				return set;
			}
		}
	}
	
	public static class IntSet
	{
		public static class Serializer extends StdSerializer<ObservableSet<Integer>>
		{
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableSet<Integer>> t) { super(t); }
		
			@Override
			public void serialize(ObservableSet<Integer> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeObject(new ArrayList<Integer>(value));
			}
			
			@Override
			public void serializeWithType(ObservableSet<Integer> value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("tagobslist");
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<ObservableSet<Integer>>
		{
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableSet<Integer>> t) { super(t); }
	
			@Override
			public ObservableSet<Integer> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<Integer> list = p.readValueAs(new TypeReference<ArrayList<Integer>>() {});
				ObservableSet<Integer> set = FXCollections.observableSet();
				for(Integer i : list) { set.add(i); }
				return set;
			}
		}
	}
}
