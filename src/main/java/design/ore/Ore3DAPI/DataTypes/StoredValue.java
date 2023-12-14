package design.ore.Ore3DAPI.DataTypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@NoArgsConstructor
public class StoredValue
{
	@Setter @Getter String value;
	@Getter boolean userViewable;
	@Getter boolean userEditable;
	
	public StoredValue(String value, boolean userViewable, boolean userEditable)
	{
		this.value = value;
		this.userViewable = userViewable;
		this.userEditable = userViewable == false ? false : userEditable;
	}
	
	public StoredValue duplicate() { return new StoredValue(value, userViewable, userEditable); }
}
