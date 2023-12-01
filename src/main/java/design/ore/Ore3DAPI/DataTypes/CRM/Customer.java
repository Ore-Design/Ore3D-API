package design.ore.Ore3DAPI.DataTypes.CRM;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import design.ore.Ore3DAPI.DataTypes.Interfaces.ValueStorageRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Customer extends ValueStorageRecord
{
	String id;
	String displayName;
	String email;
	Address shippingAddress;
	Address billingAddress;
}
