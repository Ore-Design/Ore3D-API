package design.ore.Ore3DAPI.Jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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

import design.ore.Ore3DAPI.Util.Mapper;
import design.ore.Ore3DAPI.DataTypes.StoredValue;
import design.ore.Ore3DAPI.DataTypes.Pricing.BOMEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.MiscEntry;
import design.ore.Ore3DAPI.DataTypes.Pricing.RoutingEntry;
import javafx.beans.property.SimpleDoubleProperty;

public class ComponentSerialization
{
	public class Routings
	{
		private static final String ID = "id";
		private static final String NAME = "n";
		private static final String COST_PER_QUANTITY = "cpq";
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
				gen.writeStartObject();
				gen.writeStringField(ID, value.getId());
				gen.writeStringField(NAME, value.getName());
				gen.writeNumberField(COST_PER_QUANTITY, value.getCostPerQuantity());
				gen.writeNumberField(QUANTITY, value.getUnoverriddenQuantityProperty().get());
				gen.writeNumberField(OVERRIDDEN_QUANTITY, value.getOverridenQuantityProperty().get());
				gen.writeNumberField(MARGIN, value.getMarginProperty().get());
				
				for(Entry<String, StoredValue> entry : value.getStoredValues().entrySet())
				{
					gen.writeObjectField(entry.getKey(), entry.getValue());
				}
				
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
				
				String id = null;
				String name = null;
				Double cpq = null;
				Double qty = null;
				Double ovrQty = null;
				Integer mrgn = null;
				
				Map<String, StoredValue> storedValues = new HashMap<>();
				
				Iterator<Entry<String, JsonNode>> itr = entryNode.fields();
				while(itr.hasNext())
				{
					Entry<String, JsonNode> entry = itr.next();
					switch(entry.getKey())
					{
						case ID:
							id = entry.getValue().asText();
							break;
						case NAME:
							name = entry.getValue().asText();
							break;
						case COST_PER_QUANTITY:
							cpq = entry.getValue().asDouble();
							break;
						case QUANTITY:
							qty = entry.getValue().asDouble();
							break;
						case OVERRIDDEN_QUANTITY:
							ovrQty = entry.getValue().asDouble();
							break;
						case MARGIN:
							mrgn = entry.getValue().asInt();
							break;
						default:
							storedValues.put(entry.getKey(), Mapper.getMapper().convertValue(entry.getValue(), StoredValue.class));
					}
				}
	
				RoutingEntry newEntry = new RoutingEntry(id, name, cpq, qty, mrgn, new SimpleDoubleProperty(0));
				newEntry.putStoredValues(storedValues);
				if(ovrQty != null) newEntry.getOverridenQuantityProperty().set(ovrQty);
				return newEntry;
			}
		}
	}
	
	public class BOMs
	{
		private static final String ID = "id";
		private static final String UOM = "uom";
		private static final String CUSTOM_ENTRY = "cust";
		private static final String COST_PER_QUANTITY = "cpq";
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
				// RoutingEntry(String id, String name, double qty, double overriddenQty, int margin)
				gen.writeStartObject();
				gen.writeStringField(ID, value.getId());
				gen.writeStringField(UOM, value.getUnitOfMeasure());
				gen.writeBooleanField(CUSTOM_ENTRY, value.getCustomEntryProperty().get());
				gen.writeBooleanField(IGNORE_PARENT, value.getIgnoreParentQuantityProperty().get());
				gen.writeNumberField(COST_PER_QUANTITY, value.getCostPerQuantity());
				gen.writeNumberField(QUANTITY, value.getUnoverriddenQuantityProperty().get());
				gen.writeNumberField(OVERRIDDEN_QUANTITY, value.getOverridenQuantityProperty().get());
				gen.writeNumberField(MARGIN, value.getMarginProperty().get());
				gen.writeNumberField(OVERRIDDEN_MARGIN, value.getOverridenMarginProperty().get());
				
				for(Entry<String, StoredValue> entry : value.getStoredValues().entrySet())
				{
					gen.writeObjectField(entry.getKey(), entry.getValue());
				}
				
				gen.writeEndObject();
			}
			
			@Override
			public void serializeWithType(BOMEntry value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("routing");
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
				
				String id = null;
				String uom = null;
				Double cpq = null;
				Double qty = null;
				Double ovrQty = null;
				Integer mrgn = null;
				Integer ovrMrgn = null;
				boolean ignoreParent = false;
				boolean custom = false;
				
				Map<String, StoredValue> storedValues = new HashMap<>();
				
				Iterator<Entry<String, JsonNode>> itr = entryNode.fields();
				while(itr.hasNext())
				{
					Entry<String, JsonNode> entry = itr.next();
					switch(entry.getKey())
					{
						case ID:
							id = entry.getValue().asText();
							break;
						case UOM:
							uom = entry.getValue().asText();
							break;
						case COST_PER_QUANTITY:
							cpq = entry.getValue().asDouble();
							break;
						case QUANTITY:
							qty = entry.getValue().asDouble();
							break;
						case OVERRIDDEN_QUANTITY:
							ovrQty = entry.getValue().asDouble();
							break;
						case MARGIN:
							mrgn = entry.getValue().asInt();
							break;
						case OVERRIDDEN_MARGIN:
							ovrMrgn = entry.getValue().asInt();
							break;
						case CUSTOM_ENTRY:
							custom = entry.getValue().asBoolean();
							break;
						case IGNORE_PARENT:
							ignoreParent = entry.getValue().asBoolean();
							break;
						default:
							storedValues.put(entry.getKey(), Mapper.getMapper().convertValue(entry.getValue(), StoredValue.class));
					}
				}
	
				BOMEntry newEntry = new BOMEntry(id, "", "", uom, cpq, custom, qty, mrgn, ignoreParent, new SimpleDoubleProperty(0));
				newEntry.putStoredValues(storedValues);
				if(ovrMrgn != null) newEntry.getOverridenMarginProperty().set(ovrMrgn);
				if(ovrQty != null) newEntry.getOverridenQuantityProperty().set(ovrQty);
				return newEntry;
			}
		}
	}
	
	public class Misc
	{
		private static final String NAME = "n";
		private static final String COST = "c";
		private static final String QUANTITY = "q";
		
		public static class Serializer extends StdSerializer<MiscEntry>
		{
			protected Serializer() { this(null); }
			protected Serializer(Class<MiscEntry> t) { super(t); }
		
			@Override
			public void serialize(MiscEntry value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeStartObject();
				gen.writeStringField(NAME, value.getName());
				gen.writeNumberField(COST, value.getPrice());
				gen.writeNumberField(QUANTITY, value.getQuantityProperty().get());
				
				for(Entry<String, StoredValue> entry : value.getStoredValues().entrySet())
				{
					gen.writeObjectField(entry.getKey(), entry.getValue());
				}
				
				gen.writeEndObject();
			}
			
			@Override
			public void serializeWithType(MiscEntry value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException
			{
				WritableTypeId typeId = typeSer.typeId(value, JsonToken.START_OBJECT);
				typeSer.writeTypePrefix(gen, typeId);
				gen.writeFieldName("routing");
				serialize(value, gen, provider);
				typeSer.writeTypeSuffix(gen, typeId);
			}
		}
		
		public static class Deserializer extends StdDeserializer<MiscEntry>
		{
			protected Deserializer() { this(null); }
			protected Deserializer(Class<MiscEntry> t) { super(t); }
	
			@Override
			public MiscEntry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				JsonNode entryNode = p.getCodec().readTree(p);
				
				String name = null;
				Double cost = null;
				Double qty = null;
				
				Map<String, StoredValue> storedValues = new HashMap<>();
				
				Iterator<Entry<String, JsonNode>> itr = entryNode.fields();
				while(itr.hasNext())
				{
					Entry<String, JsonNode> entry = itr.next();
					switch(entry.getKey())
					{
						case NAME:
							name = entry.getValue().asText();
							break;
						case QUANTITY:
							qty = entry.getValue().asDouble();
							break;
						case COST:
							cost = entry.getValue().asDouble();
							break;
						default:
							storedValues.put(entry.getKey(), Mapper.getMapper().convertValue(entry.getValue(), StoredValue.class));
					}
				}
	
				MiscEntry newEntry = new MiscEntry(name, cost, qty, new SimpleDoubleProperty(0));
				newEntry.putStoredValues(storedValues);
				return newEntry;
			}
		}
	}
}
