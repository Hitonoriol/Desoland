package hitonoriol.voxelsandbox.assets;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import hitonoriol.voxelsandbox.random.Random;
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
	
	public static Model emptyModel() {
		var builder = new ModelBuilder();
		builder.begin();
		return builder.end();
	}
	
	public static Model build(Mesh mesh) {
		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		builder.part("", mesh, GL20.GL_TRIANGLES, Materials.colored(Random.randomColor()));
		return builder.end();
	}
}
