package design.ore.Ore3DAPI.Jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import design.ore.Ore3DAPI.Util;
import design.ore.Ore3DAPI.DataTypes.Specs.BooleanSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.DoubleSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.EnumSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.IntSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.IntegerStringMapSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.PositiveIntSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.SearchableIntegerStringMapSpec;
import design.ore.Ore3DAPI.DataTypes.Specs.StringSpec;

public class SpecSerialization
{
	private static final String ID = "id";
	private static final String VALUE = "val";
	private static final String READ_ONLY = "ro";
	
	public static class StringSerialization
	{
		public static class Serializer extends StdSerializer<StringSpec>
		{
			private static final long serialVersionUID = 1L;
			protected Serializer() { this(null); }
			protected Serializer(Class<StringSpec> t) { super(t); }
		
			@Override
			public void serialize(StringSpec value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeFieldName(ID);
				gen.writeString(value.getId());
				gen.writeFieldName(VALUE);
				gen.writeString(value.getValue());
				gen.writeFieldName(READ_ONLY);
				gen.writeBoolean(value.isReadOnly());
			}
			
			@Override
			public void serializeWithType(StringSpec value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<StringSpec>
		{
			private static final long serialVersionUID = 1L;
			public Deserializer() { this(null); }
			protected Deserializer(Class<StringSpec> t) { super(t); }
	
			@Override
			public StringSpec deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				JsonNode specNode = p.getCodec().readTree(p);
				String id = specNode.get(ID).asText();
				String value = specNode.get(VALUE).asText();
				boolean readOnly = specNode.get(READ_ONLY).asBoolean();
				return new StringSpec(id, value, readOnly, null);
			}
		}
	}
	
	public static class PositiveIntSerialization
	{
		public static class Serializer extends StdSerializer<PositiveIntSpec>
		{
			private static final long serialVersionUID = 1L;
			protected Serializer() { this(null); }
			protected Serializer(Class<PositiveIntSpec> t) { super(t); }
		
			@Override
			public void serialize(PositiveIntSpec value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeFieldName(ID);
				gen.writeString(value.getId());
				gen.writeFieldName(VALUE);
				gen.writeNumber(value.getValue());
				gen.writeFieldName(READ_ONLY);
				gen.writeBoolean(value.isReadOnly());
			}
			
			@Override
			public void serializeWithType(PositiveIntSpec value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<PositiveIntSpec>
		{
			private static final long serialVersionUID = 1L;
			public Deserializer() { this(null); }
			protected Deserializer(Class<PositiveIntSpec> t) { super(t); }
	
			@Override
			public PositiveIntSpec deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				JsonNode specNode = p.getCodec().readTree(p);
				String id = specNode.get(ID).asText();
				int value = specNode.get(VALUE).asInt();
				boolean readOnly = specNode.get(READ_ONLY).asBoolean();
				return new PositiveIntSpec(id, value, readOnly, null);
			}
		}
	}
	
	public static class IntSerialization
	{
		public static class Serializer extends StdSerializer<IntSpec>
		{
			private static final long serialVersionUID = 1L;
			protected Serializer() { this(null); }
			protected Serializer(Class<IntSpec> t) { super(t); }
		
			@Override
			public void serialize(IntSpec value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeFieldName(ID);
				gen.writeString(value.getId());
				gen.writeFieldName(VALUE);
				gen.writeNumber(value.getValue());
				gen.writeFieldName(READ_ONLY);
				gen.writeBoolean(value.isReadOnly());
			}
			
			@Override
			public void serializeWithType(IntSpec value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<IntSpec>
		{
			private static final long serialVersionUID = 1L;
			public Deserializer() { this(null); }
			protected Deserializer(Class<IntSpec> t) { super(t); }
	
			@Override
			public IntSpec deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				JsonNode specNode = p.getCodec().readTree(p);
				String id = specNode.get(ID).asText();
				int value = specNode.get(VALUE).asInt();
				boolean readOnly = specNode.get(READ_ONLY).asBoolean();
				return new IntSpec(id, value, readOnly, null);
			}
		}
	}
	
	public static class IntStringMapSerialization
	{
		private static final String MAPIDTAG = "mapID";
		public static class Serializer extends StdSerializer<IntegerStringMapSpec>
		{
			private static final long serialVersionUID = 1L;
			protected Serializer() { this(null); }
			protected Serializer(Class<IntegerStringMapSpec> t) { super(t); }
		
			@Override
			public void serialize(IntegerStringMapSpec value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeFieldName(ID);
				gen.writeString(value.getId());
				gen.writeFieldName(VALUE);
				gen.writeNumber(value.getValue());
				gen.writeFieldName(MAPIDTAG);
				gen.writeString(value.getMapID());
				gen.writeFieldName(READ_ONLY);
				gen.writeBoolean(value.isReadOnly());
			}
			
			@Override
			public void serializeWithType(IntegerStringMapSpec value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<IntegerStringMapSpec>
		{
			private static final long serialVersionUID = 1L;
			public Deserializer() { this(null); }
			protected Deserializer(Class<IntegerStringMapSpec> t) { super(t); }
	
			@Override
			public IntegerStringMapSpec deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				JsonNode specNode = p.getCodec().readTree(p);
				String id = specNode.get(ID).asText();
				int value = specNode.get(VALUE).asInt();
				boolean readOnly = specNode.get(READ_ONLY).asBoolean();
				String mapID = specNode.get(MAPIDTAG).asText();
				return new IntegerStringMapSpec(id, mapID, value, readOnly, null);
			}
		}
	}
	
	public static class SearchableIntStringMapSerialization
	{
		private static final String MAPIDTAG = "mapID";
		public static class Serializer extends StdSerializer<SearchableIntegerStringMapSpec>
		{
			private static final long serialVersionUID = 1L;
			protected Serializer() { this(null); }
			protected Serializer(Class<SearchableIntegerStringMapSpec> t) { super(t); }
		
			@Override
			public void serialize(SearchableIntegerStringMapSpec value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeFieldName(ID);
				gen.writeString(value.getId());
				gen.writeFieldName(VALUE);
				gen.writeNumber(value.getValue());
				gen.writeFieldName(MAPIDTAG);
				gen.writeString(value.getMapID());
				gen.writeFieldName(READ_ONLY);
				gen.writeBoolean(value.isReadOnly());
			}
			
			@Override
			public void serializeWithType(SearchableIntegerStringMapSpec value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<SearchableIntegerStringMapSpec>
		{
			private static final long serialVersionUID = 1L;
			public Deserializer() { this(null); }
			protected Deserializer(Class<SearchableIntegerStringMapSpec> t) { super(t); }
	
			@Override
			public SearchableIntegerStringMapSpec deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				JsonNode specNode = p.getCodec().readTree(p);
				String id = specNode.get(ID).asText();
				int value = specNode.get(VALUE).asInt();
				boolean readOnly = specNode.get(READ_ONLY).asBoolean();
				String mapID = specNode.get(MAPIDTAG).asText();
				return new SearchableIntegerStringMapSpec(id, mapID, value, readOnly, null);
			}
		}
	}
	
	public static class BooleanSerialization
	{
		public static class Serializer extends StdSerializer<BooleanSpec>
		{
			private static final long serialVersionUID = 1L;
			protected Serializer() { this(null); }
			protected Serializer(Class<BooleanSpec> t) { super(t); }
		
			@Override
			public void serialize(BooleanSpec value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeFieldName(ID);
				gen.writeString(value.getId());
				gen.writeFieldName(VALUE);
				gen.writeBoolean(value.getValue());
				gen.writeFieldName(READ_ONLY);
				gen.writeBoolean(value.isReadOnly());
			}
			
			@Override
			public void serializeWithType(BooleanSpec value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<BooleanSpec>
		{
			private static final long serialVersionUID = 1L;
			public Deserializer() { this(null); }
			protected Deserializer(Class<BooleanSpec> t) { super(t); }
	
			@Override
			public BooleanSpec deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				JsonNode specNode = p.getCodec().readTree(p);
				String id = specNode.get(ID).asText();
				boolean value = specNode.get(VALUE).asBoolean();
				boolean readOnly = specNode.get(READ_ONLY).asBoolean();
				return new BooleanSpec(id, value, readOnly, null);
			}
		}
	}
	
	public static class DoubleSerialization
	{
		public static class Serializer extends StdSerializer<DoubleSpec>
		{
			private static final long serialVersionUID = 1L;
			protected Serializer() { this(null); }
			protected Serializer(Class<DoubleSpec> t) { super(t); }
		
			@Override
			public void serialize(DoubleSpec value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeFieldName(ID);
				gen.writeString(value.getId());
				gen.writeFieldName(VALUE);
				gen.writeNumber(value.getValue());
				gen.writeFieldName(READ_ONLY);
				gen.writeBoolean(value.isReadOnly());
			}
			
			@Override
			public void serializeWithType(DoubleSpec value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<DoubleSpec>
		{
			private static final long serialVersionUID = 1L;
			public Deserializer() { this(null); }
			protected Deserializer(Class<DoubleSpec> t) { super(t); }
	
			@Override
			public DoubleSpec deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				JsonNode specNode = p.getCodec().readTree(p);
				String id = specNode.get(ID).asText();
				double value = specNode.get(VALUE).asDouble();
				boolean readOnly = specNode.get(READ_ONLY).asBoolean();
				return new DoubleSpec(id, value, readOnly, null);
			}
		}
	}
	
	public static class EnumSerialization
	{
		private static final String ENUM_CLASS = "enumclass";
		
		public static class Serializer extends StdSerializer<EnumSpec<?>>
		{
			private static final long serialVersionUID = 1L;
			protected Serializer() { this(null); }
			protected Serializer(Class<EnumSpec<?>> t) { super(t); }
		
			@Override
			public void serialize(EnumSpec<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeFieldName(ID);
				gen.writeString(value.getId());
				gen.writeFieldName(VALUE);
				gen.writeObject(value.getValue());
				gen.writeFieldName(ENUM_CLASS);
				gen.writeString(value.getClazz().getName());
				gen.writeFieldName(READ_ONLY);
				gen.writeBoolean(value.isReadOnly());
			}
			
			@Override
			public void serializeWithType(EnumSpec<?> value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<EnumSpec<?>>
		{
			private static final long serialVersionUID = 1L;
			public Deserializer() { this(null); }
			protected Deserializer(Class<EnumSpec<?>> t) { super(t); }
	
			@Override
			public EnumSpec<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				JsonNode specNode = p.getCodec().readTree(p);
				String id = specNode.get(ID).asText();
				
				String classString = specNode.get(ENUM_CLASS).asText();
				
				Class clazz = null;
				
				for(ClassLoader cl : Util.getRegisteredClassLoaders())
				{
					try { clazz = Class.forName(classString, true, cl); break; }
					catch (ClassNotFoundException e) {}
				}
				
				Enum<?> value = Enum.valueOf(clazz, specNode.get(VALUE).asText());
				
				boolean readOnly = specNode.get(READ_ONLY).asBoolean();
				
				EnumSpec<?> s = new EnumSpec<>();
				s.setId(id);
				s.setValue(value);
				s.setClazz(clazz);
				s.setReadOnly(readOnly);
				
				return s;
			}
		}
	}
}
