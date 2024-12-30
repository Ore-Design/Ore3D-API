package design.ore.api.ore3d.data.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

import design.ore.api.ore3d.data.core.Build;

public interface ISummaryOption
{
	@JsonIgnore public String getSearchName();
	@JsonIgnore public Object getSummaryValue();
	@JsonIgnore public Build getParentBuild();
}
