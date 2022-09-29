package hitonoriol.voxelsandbox.assets;

import com.badlogic.gdx.Gdx;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Models {
	public static final SceneAsset player = loadModel("models/player/player.gltf");
	
	public static SceneAsset loadModel(String modelPath) {
		return new GLTFLoader().load(Gdx.files.internal(modelPath), true);
	}
	
	public static Scene create(SceneAsset modelAsset) {
		return new Scene(modelAsset.scene);
	}
}
