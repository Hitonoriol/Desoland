package hitonoriol.voxelsandbox.assets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;

import hitonoriol.voxelsandbox.random.Random;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class Materials {
	public static Material colored(Color color) {
		return new Material(PBRColorAttribute.createBaseColorFactor(Random.randomColor()));
	}
}
