package hitonoriol.voxelsandbox.assets;

import com.badlogic.gdx.assets.AssetManager;

import net.mgsx.gltf.loaders.glb.GLBAssetLoader;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Assets {
	private static final AssetManager manager = new AssetManager();
	static {
		manager.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
		manager.setLoader(SceneAsset.class, ".glb", new GLBAssetLoader());
	}

	private Assets() {}

	public static <T> T load(String fileName, Class<T> type) {
		manager.load(fileName, type);
		return manager.finishLoadingAsset(fileName);
	}

	public static AssetManager manager() {
		return manager;
	}
}
