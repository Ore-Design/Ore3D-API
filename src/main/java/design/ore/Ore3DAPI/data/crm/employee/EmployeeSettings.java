package design.ore.Ore3DAPI.data.crm.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSettings
{
	boolean displayNewBuildsInTreeMode = false;
	boolean displayNavigationTree = true;
	int autosaveInterval = 60;
}
