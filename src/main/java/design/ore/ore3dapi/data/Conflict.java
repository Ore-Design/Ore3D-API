package design.ore.ore3dapi.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Conflict
{
	int buildUID;
	String message;
	boolean isWarning;
	
	@Override
	public String toString() { return "Conflict for Build " + buildUID + ": " + message + " (Is Warning?: " + isWarning + ")"; }
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == this) return true;
		
		if(!(obj instanceof Conflict)) return false;
		
		Conflict conf = (Conflict) obj;
		
		return buildUID == conf.buildUID && message.equals(conf.message) && isWarning == conf.isWarning;
	}
}
