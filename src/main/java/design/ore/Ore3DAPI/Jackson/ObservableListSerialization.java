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

import design.ore.Ore3DAPI.DataTypes.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Conflict;
import design.ore.Ore3DAPI.DataTypes.RoutingEntry;
import design.ore.Ore3DAPI.DataTypes.Tag;
import design.ore.Ore3DAPI.DataTypes.Records.Build;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ObservableListSerialization
{
	public static class BOMEntryList
	{
		public static class Serializer extends StdSerializer<ObservableList<BOMEntry>>
		{
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<BOMEntry>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<BOMEntry> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeObject(new ArrayList<BOMEntry>(value));
			}
			
			@Override
			public void serializeWithType(ObservableList<BOMEntry> value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("bomssobslist");
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<ObservableList<BOMEntry>>
		{
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<BOMEntry>> t) { super(t); }
	
			@Override
			public ObservableList<BOMEntry> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<BOMEntry> list = p.readValueAs(new TypeReference<ArrayList<BOMEntry>>() {});
				return FXCollections.observableArrayList(list);
			}
		}
	}
	public static class RoutingEntryList
	{
		public static class Serializer extends StdSerializer<ObservableList<RoutingEntry>>
		{
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<RoutingEntry>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<RoutingEntry> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeObject(new ArrayList<RoutingEntry>(value));
			}
			
			@Override
			public void serializeWithType(ObservableList<RoutingEntry> value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("routingsobslist");
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<ObservableList<RoutingEntry>>
		{
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<RoutingEntry>> t) { super(t); }
	
			@Override
			public ObservableList<RoutingEntry> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<RoutingEntry> list = p.readValueAs(new TypeReference<ArrayList<RoutingEntry>>() {});
				return FXCollections.observableArrayList(list);
			}
		}
	}
	public static class BuildList
	{
		public static class Serializer extends StdSerializer<ObservableList<Build>>
		{
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<Build>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<Build> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeStartArray();
				for(Build b : value)
				{
					gen.writeObject(b);
				}
				gen.writeEndArray();
//				gen.writeObject(new ArrayList<Build>(value));
			}
			
			@Override
			public void serializeWithType(ObservableList<Build> value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("buildobslist");
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<ObservableList<Build>>
		{
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<Build>> t) { super(t); }
	
			@Override
			public ObservableList<Build> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<Build> list = p.readValueAs(new TypeReference<ArrayList<Build>>() {});
				return FXCollections.observableArrayList(list);
			}
		}
	}
	public static class ConflictList
	{
		public static class Serializer extends StdSerializer<ObservableList<Conflict>>
		{
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<Conflict>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<Conflict> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeObject(new ArrayList<Conflict>(value));
			}
			
			@Override
			public void serializeWithType(ObservableList<Conflict> value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("cnflctobslist");
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<ObservableList<Conflict>>
		{
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<Conflict>> t) { super(t); }
	
			@Override
			public ObservableList<Conflict> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<Conflict> list = p.readValueAs(new TypeReference<ArrayList<Conflict>>() {});
				return FXCollections.observableArrayList(list);
			}
		}
	}
	public static class TagList
	{
		public static class Serializer extends StdSerializer<ObservableList<Tag>>
		{
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<Tag>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<Tag> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeObject(new ArrayList<Tag>(value));
			}
			
			@Override
			public void serializeWithType(ObservableList<Tag> value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("tagobslist");
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<ObservableList<Tag>>
		{
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<Tag>> t) { super(t); }
	
			@Override
			public ObservableList<Tag> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<Tag> list = p.readValueAs(new TypeReference<ArrayList<Tag>>() {});
				return FXCollections.observableArrayList(list);
			}
		}
	}
}
