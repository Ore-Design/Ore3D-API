package design.ore.Ore3DAPI.data.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.Ore3DAPI.data.core.Build;

public interface ISummaryOption
{
	@JsonIgnore public String getSearchName();
	@JsonIgnore public Object getSummaryValue();
	@JsonIgnore public Build getParentBuild();
}
