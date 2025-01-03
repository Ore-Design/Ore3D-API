package design.ore.ore3dapi.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PropertySerialization
{
	public static class StringSer
	{
		public static class Serializer extends StdSerializer<StringProperty>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<StringProperty> t) { super(t); }
		
			@Override
			public void serialize(StringProperty value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeString(value.getValue());
			}
			
			@Override
			public void serializeWithType(StringProperty value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<StringProperty>
		{
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<StringProperty> t) { super(t); }
	
			@Override
			public StringProperty deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				String value = p.getCodec().readValue(p, String.class);
				return new SimpleStringProperty(value);
			}
	
			@Override
			public StringProperty deserialize(JsonParser p, DeserializationContext ctxt, StringProperty prop) throws IOException, JacksonException
			{
				String value = p.getCodec().readValue(p, String.class);
				prop.setValue(value);
				return prop;
			}
		}
	}
	public static class ReadOnlyStringSer
	{
		public static class Serializer extends StdSerializer<ReadOnlyStringWrapper>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<ReadOnlyStringWrapper> t) { super(t); }
		
			@Override
			public void serialize(ReadOnlyStringWrapper value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeString(value.getValue());
			}
			
			@Override
			public void serializeWithType(ReadOnlyStringWrapper value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<ReadOnlyStringWrapper>
		{
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<ReadOnlyStringWrapper> t) { super(t); }
	
			@Override
			public ReadOnlyStringWrapper deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				String value = p.getCodec().readValue(p, String.class);
				return new ReadOnlyStringWrapper(value);
			}
	
			@Override
			public ReadOnlyStringWrapper deserialize(JsonParser p, DeserializationContext ctxt, ReadOnlyStringWrapper prop) throws IOException, JacksonException
			{
				String value = p.getCodec().readValue(p, String.class);
				prop.setValue(value);
				return prop;
			}
		}
	}
	
	public static class BooleanSer
	{
		public static class Serializer extends StdSerializer<BooleanProperty>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<BooleanProperty> t) { super(t); }
		
			@Override
			public void serialize(BooleanProperty value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeBoolean(value.getValue());
			}
			
			@Override
			public void serializeWithType(BooleanProperty value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<BooleanProperty>
		{
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<BooleanProperty> t) { super(t); }
	
			@Override
			public BooleanProperty deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				Boolean value = p.getCodec().readValue(p, Boolean.class);
				return new SimpleBooleanProperty(value);
			}
	
			@Override
			public BooleanProperty deserialize(JsonParser p, DeserializationContext ctxt, BooleanProperty prop) throws IOException, JacksonException
			{
				Boolean value = p.getCodec().readValue(p, Boolean.class);
				prop.setValue(value);
				return prop;
			}
		}
	}
	
	public static class DoubleSer
	{
		public static class Serializer extends StdSerializer<DoubleProperty>
		{
			private static final long serialVersionUID = 1L;
			
			protected Serializer() { this(null); }
			protected Serializer(Class<DoubleProperty> t) { super(t); }
		
			@Override
			public void serialize(DoubleProperty value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeNumber(value.getValue());
			}
			
			@Override
			public void serializeWithType(DoubleProperty value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<DoubleProperty>
		{
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<DoubleProperty> t) { super(t); }
	
			@Override
			public DoubleProperty deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				Double value = p.getCodec().readValue(p, Double.class);
				return new SimpleDoubleProperty(value);
			}
	
			@Override
			public DoubleProperty deserialize(JsonParser p, DeserializationContext ctxt, DoubleProperty prop) throws IOException, JacksonException
			{
				Double value = p.getCodec().readValue(p, Double.class);
				prop.setValue(value);
				return prop;
			}
		}
	}
}
