package design.ore.api.ore3d.data.crm;

import com.fasterxml.jackson.annotation.JsonFormat;
import design.ore.api.ore3d.data.interfaces.ValueStorageRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Customer extends ValueStorageRecord
{
	String id;
	String displayName;
	String email;
	Address shippingAddress;
	Address billingAddress;
}
