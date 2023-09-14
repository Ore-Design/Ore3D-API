package design.ore.Ore3DAPI.DataTypes;

import java.util.List;

import design.ore.Ore3DAPI.DataTypes.Records.Build;
import javafx.scene.image.Image;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductFamily
{
	String name;
	Image thumbnail;
	List<? extends Build> builds;
}
