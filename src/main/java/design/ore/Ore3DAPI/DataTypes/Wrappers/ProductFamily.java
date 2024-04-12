package design.ore.Ore3DAPI.DataTypes.Wrappers;

import java.util.List;

import design.ore.Ore3DAPI.DataTypes.Protected.Build;
import javafx.scene.image.Image;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ProductFamily
{
	@Getter String name;
	@Getter Image thumbnail;
	@Getter List<Pair<String, Build>> builds;
}
