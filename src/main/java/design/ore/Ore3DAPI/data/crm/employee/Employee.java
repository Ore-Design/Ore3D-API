package design.ore.Ore3DAPI.data.crm.employee;

import design.ore.Ore3DAPI.data.interfaces.ValueStorageRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Employee extends ValueStorageRecord
{
	String id;
	EmployeeSettings settings;
}
