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
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import design.ore.Ore3DAPI.DataTypes.RoutingEntry;

public class RoutingSerialization
{
	private static final String ID = "id";
	private static final String NAME = "n";
	private static final String QUANTITY = "q";
	private static final String OVERRIDDEN_QUANTITY = "ovrq";
	private static final String MARGIN = "m";
	
	public static class Serializer extends StdSerializer<RoutingEntry>
	{
		protected Serializer() { this(null); }
		protected Serializer(Class<RoutingEntry> t) { super(t); }
	
		@Override
		public void serialize(RoutingEntry value, JsonGenerator gen, SerializerProvider provider) throws IOException
		{
			// RoutingEntry(String id, String name, double qty, double overriddenQty, int margin)
			gen.writeStartObject();
			gen.writeStringField(ID, value.getId());
			gen.writeStringField(NAME, value.getName());
			gen.writeNumberField(QUANTITY, value.getUnoverriddenQuantityProperty().get());
			gen.writeNumberField(OVERRIDDEN_QUANTITY, value.getOverridenQuantityProperty().get());
			gen.writeNumberField(MARGIN, value.getMarginProperty().get());
			gen.writeEndObject();
		}
		
		@Override
		public void serializeWithType(RoutingEntry value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
		{
			WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
			typeSer.writeTypePrefix(gen, typeId);
			gen.writeFieldName("routing");
			serialize(value, gen, provider);
			typeSer.writeTypeSuffix(gen, typeId);
		}
	}
	
	public static class Deserializer extends StdDeserializer<RoutingEntry>
	{
		protected Deserializer() { this(null); }
		protected Deserializer(Class<RoutingEntry> t) { super(t); }

		@Override
		public RoutingEntry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
		{
			JsonNode entryNode = p.getCodec().readTree(p);
			String id = entryNode.get(ID).asText();
			String name = entryNode.get(NAME).asText();
			double qty = ((DoubleNode) entryNode.get(QUANTITY)).asDouble();
			double ovrQty = ((DoubleNode) entryNode.get(OVERRIDDEN_QUANTITY)).asDouble();
			int mrgn = entryNode.get(MARGIN).asInt();
			
			return new RoutingEntry(id, name, qty, ovrQty, mrgn);
		}
	}
}
