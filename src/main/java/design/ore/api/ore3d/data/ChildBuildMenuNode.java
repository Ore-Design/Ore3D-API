package design.ore.api.ore3d.data;

import java.util.ArrayList;
import java.util.List;

import design.ore.api.ore3d.data.core.Build;
import lombok.Getter;

public class ChildBuildMenuNode
{
	@Getter String label;
	@Getter Build build;
	@Getter List<ChildBuildMenuNode> children = new ArrayList<>();
	
	public ChildBuildMenuNode(String label, Build build)
	{
		this.label = label;
		this.build = build;
	}
	
	public ChildBuildMenuNode(String label, List<ChildBuildMenuNode> children)
	{
		this.label = label;
		this.children = children;
	}
}
