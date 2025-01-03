package design.ore.ore3dapi.data.crm.employee;

import design.ore.ore3dapi.data.interfaces.ValueStorageRecord;
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
