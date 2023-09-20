package design.ore.Ore3DAPI.DataTypes;

import java.util.List;

import design.ore.Ore3DAPI.DataTypes.Records.Build;
import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ProductFamily
{
	@Getter String name;
	@Getter Image thumbnail;
	@Getter List<? extends Build> builds;
}
