package design.ore.Ore3DAPI.Jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import design.ore.Ore3DAPI.DataTypes.Protected.BuildPrice;

public class BuildDataSerialization
{
	public static class BuildPriceSer
	{
		private final static String UNIT_PRICE = "overridenUnitPrice";
		private final static String TOTAL_PRICE = "overridenTotalPrice";
		
		public static class Serializer extends StdSerializer<BuildPrice>
		{
			private static final long serialVersionUID = 1L;

			protected Serializer() { this(null); }
			protected Serializer(Class<BuildPrice> t) { super(t); }
		
			@Override
			public void serialize(BuildPrice value, JsonGenerator gen, SerializerProvider provider) throws IOException
			{
				gen.writeStartObject();
				if(value.getUnitPriceOverriddenProperty().get()) gen.writeNumberField(UNIT_PRICE, value.getOverriddenUnitPrice().get());
				if(value.getTotalPriceOverriddenProperty().get()) gen.writeNumberField(TOTAL_PRICE, value.getOverriddenTotalPrice().get());
				gen.writeEndObject();
			}
		}
		
		public static class Deserializer extends StdDeserializer<BuildPrice>
		{
			private static final long serialVersionUID = 1L;
			
			public Deserializer() { this(null); }
			protected Deserializer(Class<BuildPrice> t) { super(t); }
	
			@Override
			public BuildPrice deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
			{
				throw new IllegalArgumentException ("BuildPrice values should only be deserialized via merging (@JsonMerge)!");
			}
	
			@Override
			public BuildPrice deserialize(JsonParser p, DeserializationContext ctxt, BuildPrice prop) throws IOException, JacksonException
			{
				JsonNode entryNode = p.getCodec().readTree(p);
				
				double overriddenUnitPrice = -Double.MAX_VALUE;
				double overriddenTotalPrice = -Double.MAX_VALUE;
				
				if(entryNode.has(UNIT_PRICE) && entryNode.get(UNIT_PRICE).isDouble()) overriddenUnitPrice = entryNode.get(UNIT_PRICE).asDouble();
				if(entryNode.has(TOTAL_PRICE) && entryNode.get(TOTAL_PRICE).isDouble()) overriddenTotalPrice = entryNode.get(TOTAL_PRICE).asDouble();
				
				prop.overrideUnitPrice(overriddenUnitPrice);
				prop.overrideTotalPrice(overriddenTotalPrice);
				return prop;
			}
		}
	}
}
