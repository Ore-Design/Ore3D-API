package design.ore.Ore3DAPI.DataTypes.CRM;

import java.util.Map;

import design.ore.Ore3DAPI.DataTypes.Interfaces.ValueStorageRecord;
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
	Map<String, String> settings;
}
