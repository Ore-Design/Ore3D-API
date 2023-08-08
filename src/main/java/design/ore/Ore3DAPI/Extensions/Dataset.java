package design.ore.Ore3DAPI.Extensions;

import java.util.List;

import org.pf4j.ExtensionPoint;

import design.ore.Ore3DAPI.Records.Build;

public interface Dataset extends ExtensionPoint
{
	List<? extends Build> retrieveCatalog();
}
