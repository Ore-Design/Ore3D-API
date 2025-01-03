package design.ore.ore3dapi.data.wrappers;

import java.util.List;

import design.ore.ore3dapi.data.core.Build;
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
