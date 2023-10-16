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
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import design.ore.Ore3DAPI.DataTypes.BOMEntry;

public class BOMSerialization
{
	private static final String ID = "id";
	private static final String CUSTOM_ENTRY = "cust";
	private static final String QUANTITY = "q";
	private static final String OVERRIDDEN_QUANTITY = "ovrq";
	private static final String MARGIN = "m";
	private static final String OVERRIDDEN_MARGIN = "ovrm";
	private static final String IGNORE_PARENT = "ipq";
	
	public static class Serializer extends StdSerializer<BOMEntry>
	{
		protected Serializer() { this(null); }
		protected Serializer(Class<BOMEntry> t) { super(t); }
	
		@Override
		public void serialize(BOMEntry value, JsonGenerator gen, SerializerProvider provider) throws IOException
		{
			gen.writeStartObject();
			gen.writeStringField(ID, value.getId());
			gen.writeBooleanField(CUSTOM_ENTRY, value.getCustomEntryProperty().get());
			gen.writeNumberField(QUANTITY, value.getUnoverriddenQuantityProperty().get());
			gen.writeNumberField(OVERRIDDEN_QUANTITY, value.getOverridenQuantityProperty().get());
			gen.writeNumberField(MARGIN, value.getUnoverriddenMarginProperty().get());
			gen.writeNumberField(OVERRIDDEN_MARGIN, value.getOverridenMarginProperty().get());
			gen.writeBooleanField(IGNORE_PARENT, value.getIgnoreParentQuantityProperty().get());
			gen.writeEndObject();
		}
		
		@Override
		public void serializeWithType(BOMEntry value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
		{
			WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
			typeSer.writeTypePrefix(gen, typeId);
			gen.writeFieldName("bomentrytype");
			serialize(value, gen, provider);
			typeSer.writeTypeSuffix(gen, typeId);
		}
	}
	
	public static class Deserializer extends StdDeserializer<BOMEntry>
	{
		protected Deserializer() { this(null); }
		protected Deserializer(Class<BOMEntry> t) { super(t); }

		@Override
		public BOMEntry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
		{
			JsonNode entryNode = p.getCodec().readTree(p);
			String id = entryNode.get(ID).asText();
			boolean customEntry = ((BooleanNode) entryNode.get(CUSTOM_ENTRY)).asBoolean();
			double qty = ((DoubleNode) entryNode.get(QUANTITY)).asDouble();
			double ovrQty = ((DoubleNode) entryNode.get(OVERRIDDEN_QUANTITY)).asDouble();
			int mrgn = ((IntNode) entryNode.get(MARGIN)).asInt();
			int ovrMrgn = ((IntNode) entryNode.get(OVERRIDDEN_MARGIN)).asInt();
			boolean ignoreParent = ((BooleanNode) entryNode.get(IGNORE_PARENT)).asBoolean();
			
			return new BOMEntry(id, customEntry, ignoreParent, qty, ovrQty, mrgn, ovrMrgn);
		}
	}
}
