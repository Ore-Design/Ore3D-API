package design.ore.api.ore3d.data.wrappers;

import design.ore.api.ore3d.data.core.Build;
import javafx.scene.image.Image;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class ProductFamily
{
	@Getter String name;
	@Getter Image thumbnail;
	@Getter List<Pair<String, Build>> builds;
}
