package design.ore.api.ore3d.data.crm.employee;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class EmployeeSettings
{
	int autosaveInterval = 60;
	Map<String, String> serializedBOMSummaryTemplates = new HashMap<>();
	Map<String, String> serializedRoutingSummaryTemplates = new HashMap<>();
	Map<String, String> serializedSpecSummaryTemplates = new HashMap<>();
}
