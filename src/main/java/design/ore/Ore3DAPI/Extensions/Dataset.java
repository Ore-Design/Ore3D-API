package design.ore.Ore3DAPI.Extensions;

import java.util.List;

import org.pf4j.ExtensionPoint;

import design.ore.Ore3D.API.CoreTypes.APIEndpoint;
import design.ore.Ore3D.API.CoreTypes.Build;

public interface Dataset extends ExtensionPoint
{
	List<? extends Build> retrieveCatalog();
	APIEndpoint getEnpoint();
	boolean postInitialize();
}
