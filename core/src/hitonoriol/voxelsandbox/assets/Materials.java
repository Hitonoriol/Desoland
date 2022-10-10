package hitonoriol.voxelsandbox.assets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import hitonoriol.voxelsandbox.random.Random;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class Materials {
	public static Material colored(Color color) {
		return new Material(
				new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, color)
		);
	}
	
	public static Material reflective(Color color) {
		var material = colored(color);
		material.set(new PBRColorAttribute(PBRColorAttribute.Reflection, color));
		return material;
	}
}
