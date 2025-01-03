package design.ore.ore3dapi.data.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.ore3dapi.data.core.Build;

public interface ISummaryOption
{
	@JsonIgnore public String getSearchName();
	@JsonIgnore public Object getSummaryValue();
	@JsonIgnore public Build getParentBuild();
}
