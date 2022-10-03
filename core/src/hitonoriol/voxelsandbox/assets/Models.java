package hitonoriol.voxelsandbox.assets;

import com.badlogic.gdx.graphics.g3d.Model;

import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Models {
	public static final Model player = loadModel("models/player/player.gltf");
	
	public static Model loadModel(String modelPath) {
		return Assets.load(modelPath, SceneAsset.class).scene.model;
	}
	
	public static Scene create(SceneAsset modelAsset) {
		return new Scene(modelAsset.scene);
	}
}
