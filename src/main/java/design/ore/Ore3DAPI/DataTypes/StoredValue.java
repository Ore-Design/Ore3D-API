package design.ore.Ore3DAPI.DataTypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.Ore3DAPI.Jackson.PropertySerialization;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@NoArgsConstructor
public class StoredValue
{
	@JsonSerialize(using = PropertySerialization.StringSer.Serializer.class)
	@JsonDeserialize(using = PropertySerialization.StringSer.Deserializer.class)
	@JsonMerge @Getter SimpleStringProperty valueProperty = new SimpleStringProperty();
	public String getValue() { return valueProperty.getValue(); }
	@Getter boolean userViewable;
	@Getter boolean userEditable;
	
	public StoredValue(String value, boolean userViewable, boolean userEditable)
	{
		valueProperty.setValue(value);
		this.userViewable = userViewable;
		this.userEditable = userViewable == false ? false : userEditable;
	}
	
	public StoredValue duplicate() { return new StoredValue(valueProperty.getValue(), userViewable, userEditable); }
}
