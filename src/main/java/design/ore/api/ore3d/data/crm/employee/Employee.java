package design.ore.api.ore3d.data.crm.employee;

import design.ore.api.ore3d.data.interfaces.ValueStorageRecord;
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
