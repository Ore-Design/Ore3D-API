package design.ore.ore3dapi.jackson;

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

import design.ore.ore3dapi.data.Conflict;
import design.ore.ore3dapi.data.core.Build;
import design.ore.ore3dapi.data.core.Tag;
import design.ore.ore3dapi.data.pricing.BOMEntry;
import design.ore.ore3dapi.data.pricing.MiscEntry;
import design.ore.ore3dapi.data.pricing.RoutingEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ObservableListSerialization
{
	public static class BOMEntryList
	{
		public static class Serializer extends StdSerializer<ObservableList<BOMEntry>>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<BOMEntry>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<BOMEntry> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeStartArray();
				for(BOMEntry e : value) { gen.writeObject(e); }
				gen.writeEndArray();
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
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<BOMEntry>> t) { super(t); }
	
			@Override
			public ObservableList<BOMEntry> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<BOMEntry> list = p.readValueAs(new TypeReference<ArrayList<BOMEntry>>() {});
				return FXCollections.observableArrayList(list);
			}
	
			@Override
			public ObservableList<BOMEntry> deserialize(JsonParser p, DeserializationContext ctxt, ObservableList<BOMEntry> merge) throws IOException, JacksonException
			{	
				ArrayList<BOMEntry> list = p.readValueAs(new TypeReference<ArrayList<BOMEntry>>() {});
				merge.setAll(list);
				return merge;
			}
		}
	}

	public static class RoutingEntryList
	{
		public static class Serializer extends StdSerializer<ObservableList<RoutingEntry>>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<RoutingEntry>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<RoutingEntry> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeStartArray();
				for(RoutingEntry e : value) { gen.writeObject(e); }
				gen.writeEndArray();
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
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<RoutingEntry>> t) { super(t); }
	
			@Override
			public ObservableList<RoutingEntry> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<RoutingEntry> list = p.readValueAs(new TypeReference<ArrayList<RoutingEntry>>() {});
				return FXCollections.observableArrayList(list);
			}
	
			@Override
			public ObservableList<RoutingEntry> deserialize(JsonParser p, DeserializationContext ctxt, ObservableList<RoutingEntry> merge) throws IOException, JacksonException
			{	
				ArrayList<RoutingEntry> list = p.readValueAs(new TypeReference<ArrayList<RoutingEntry>>() {});
				merge.setAll(list);
				return merge;
			}
		}
	}
	
	public static class MiscEntryList
	{
		public static class Serializer extends StdSerializer<ObservableList<MiscEntry>>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<MiscEntry>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<MiscEntry> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeStartArray();
				for(MiscEntry e : value) { gen.writeObject(e); }
				gen.writeEndArray();
			}
			
			@Override
			public void serializeWithType(ObservableList<MiscEntry> value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("routingsobslist");
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<ObservableList<MiscEntry>>
		{
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<MiscEntry>> t) { super(t); }
	
			@Override
			public ObservableList<MiscEntry> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<MiscEntry> list = p.readValueAs(new TypeReference<ArrayList<MiscEntry>>() {});
				return FXCollections.observableArrayList(list);
			}
	
			@Override
			public ObservableList<MiscEntry> deserialize(JsonParser p, DeserializationContext ctxt, ObservableList<MiscEntry> merge) throws IOException, JacksonException
			{	
				ArrayList<MiscEntry> list = p.readValueAs(new TypeReference<ArrayList<MiscEntry>>() {});
				merge.setAll(list);
				return merge;
			}
		}
	}
	
	public static class BuildList
	{
		public static class Serializer extends StdSerializer<ObservableList<Build>>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<Build>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<Build> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeStartArray();
				for(Build b : value) { gen.writeObject(b); }
				gen.writeEndArray();
			}
		}
		
		public static class Deserializer extends StdDeserializer<ObservableList<Build>>
		{
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<Build>> t) { super(t); }
	
			@Override
			public ObservableList<Build> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<Build> list = p.readValueAs(new TypeReference<ArrayList<Build>>() {});
				return FXCollections.observableArrayList(list);
			}
	
			@Override
			public ObservableList<Build> deserialize(JsonParser p, DeserializationContext ctxt, ObservableList<Build> originalList) throws IOException, JacksonException
			{	
				ArrayList<Build> list = p.readValueAs(new TypeReference<ArrayList<Build>>() {});
				originalList.addAll(list);
				return originalList;
			}
		}
	}
	public static class ConflictList
	{
		public static class Serializer extends StdSerializer<ObservableList<Conflict>>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<Conflict>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<Conflict> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeStartArray();
				for(Conflict e : value) { gen.writeObject(e); }
				gen.writeEndArray();
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
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<Conflict>> t) { super(t); }
	
			@Override
			public ObservableList<Conflict> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<Conflict> list = p.readValueAs(new TypeReference<ArrayList<Conflict>>() {});
				return FXCollections.observableArrayList(list);
			}
	
			@Override
			public ObservableList<Conflict> deserialize(JsonParser p, DeserializationContext ctxt, ObservableList<Conflict> merge) throws IOException, JacksonException
			{	
				ArrayList<Conflict> list = p.readValueAs(new TypeReference<ArrayList<Conflict>>() {});
				merge.setAll(list);
				return merge;
			}
		}
	}
	
	public static class TagList
	{
		public static class Serializer extends StdSerializer<ObservableList<Tag>>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<Tag>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<Tag> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeStartArray();
				for(Tag e : value) { gen.writeObject(e); }
				gen.writeEndArray();
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
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<Tag>> t) { super(t); }
	
			@Override
			public ObservableList<Tag> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<Tag> list = p.readValueAs(new TypeReference<ArrayList<Tag>>() {});
				return FXCollections.observableArrayList(list);
			}
	
			@Override
			public ObservableList<Tag> deserialize(JsonParser p, DeserializationContext ctxt, ObservableList<Tag> merge) throws IOException, JacksonException
			{	
				ArrayList<Tag> list = p.readValueAs(new TypeReference<ArrayList<Tag>>() {});
				merge.setAll(list);
				return merge;
			}
		}
	}

	public static class IntList
	{
		public static class Serializer extends StdSerializer<ObservableList<Integer>>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<ObservableList<Integer>> t) { super(t); }
		
			@Override
			public void serialize(ObservableList<Integer> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeStartArray();
				for(Integer e : value) { gen.writeObject(e); }
				gen.writeEndArray();
			}
			
			@Override
			public void serializeWithType(ObservableList<Integer> value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("tagobslist");
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<ObservableList<Integer>>
		{
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<ObservableList<Integer>> t) { super(t); }
	
			@Override
			public ObservableList<Integer> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{	
				ArrayList<Integer> list = p.readValueAs(new TypeReference<ArrayList<Integer>>() {});
				return FXCollections.observableArrayList(list);
			}
	
			@Override
			public ObservableList<Integer> deserialize(JsonParser p, DeserializationContext ctxt, ObservableList<Integer> merge) throws IOException, JacksonException
			{	
				ArrayList<Integer> list = p.readValueAs(new TypeReference<ArrayList<Integer>>() {});
				merge.setAll(list);
				return merge;
			}
		}
	}
}
