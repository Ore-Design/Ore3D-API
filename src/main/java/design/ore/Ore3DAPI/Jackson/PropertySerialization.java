package design.ore.Ore3DAPI.Jackson;

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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PropertySerialization
{
	public static class StringSer
	{
		private static final String VALUE = "val";
		
		public static class Serializer extends StdSerializer<StringProperty>
		{
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
			public Deserializer() { this(null); }
			protected Deserializer(Class<StringProperty> t) { super(t); }
	
			@Override
			public StringProperty deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				String value = p.getCodec().readValue(p, String.class);
				return new SimpleStringProperty(value);
			}
		}
	}
}
