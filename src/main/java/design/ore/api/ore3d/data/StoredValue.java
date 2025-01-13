package design.ore.api.ore3d.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import design.ore.api.ore3d.jackson.PropertySerialization;
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
	@Getter boolean dataRequired;
	
	public StoredValue(String value, boolean userViewable, boolean userEditable, boolean dataRequired)
	{
		valueProperty.setValue(value);
		this.userViewable = userViewable;
		this.userEditable = userViewable == false ? false : userEditable;
		this.dataRequired = dataRequired;
	}
	
	public StoredValue(String value, boolean userViewable, boolean userEditable) { this(value, userViewable, userEditable, false); }
	
	public StoredValue duplicate() { return new StoredValue(valueProperty.getValue(), userViewable, userEditable, dataRequired); }
}
