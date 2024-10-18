package design.ore.Ore3DAPI.data.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ISummaryOption
{
	@JsonIgnore public String getSearchName();
	@JsonIgnore public Object getSummaryValue();
}
