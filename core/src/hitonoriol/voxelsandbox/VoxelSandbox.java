package hitonoriol.voxelsandbox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Vector3;

import hitonoriol.voxelsandbox.entity.Player;
import hitonoriol.voxelsandbox.io.Out;
import hitonoriol.voxelsandbox.random.Random;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class VoxelSandbox extends ApplicationAdapter {

	private SceneManager sceneManager;
	private Cubemap diffuseCubemap;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private SceneSkybox skybox;
	private DirectionalLightEx light;

	private Vector3 tmpVec = new Vector3();

	private Player player;

	@Override
	public void create() {
		sceneManager = new SceneManager(1000);
		player = new Player();
		sceneManager.addScene(player);
		sceneManager.setCamera(player.getCamera());

		light = new DirectionalLightEx();
		light.direction.set(1, -3, 1).nor();
		light.color.set(Color.WHITE);
		sceneManager.environment.add(light);

		IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
		environmentCubemap = iblBuilder.buildEnvMap(1024);
		diffuseCubemap = iblBuilder.buildIrradianceMap(256);
		specularCubemap = iblBuilder.buildRadianceMap(10);
		iblBuilder.dispose();

		sceneManager.setAmbientLight(1f);
		Texture brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
		sceneManager.environment.set(new ColorAttribute(ColorAttribute.Fog, Color.WHITE));

		skybox = new SceneSkybox(environmentCubemap);
		sceneManager.setSkyBox(skybox);
		setUpScene();
	}

	private void setUpScene() {
		float d = 100f;
		for (int i = 0; i < 50; ++i)
			buildBox(Random.nextFloat(-d, d), 0f, Random.nextFloat(-d, d), Random.nextFloat(3, 20));
	}

	private void buildBox(float x, float y, float z, float size) {
		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		Material material = new Material();
		Color color = new Color(Random.nextFloat(0, 1), Random.nextFloat(0, 1), Random.nextFloat(0, 1), 1);
		material.set(PBRColorAttribute.createBaseColorFactor(color));
		MeshPartBuilder partBuilder = builder.part(x + ", " + y + ", " + z, GL20.GL_TRIANGLES,
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, material);
		BoxShapeBuilder.build(partBuilder, x, y, z, size, size, size);

		ModelInstance model = new ModelInstance(builder.end());
		sceneManager.addScene(new Scene(model));
	}

	@Override
	public void resize(int width, int height) {
		sceneManager.updateViewport(width, height);
	}

	@Override
	public void render() {
		float delta = Gdx.graphics.getDeltaTime();
		processMovement(delta);
		pollKeybinds();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		sceneManager.update(delta);
		sceneManager.render();
	}

	private void processMovement(float delta) {
		float moveBy = player.getMovementSpeed() * delta;
		Vector3 moveTranslation = player.getMoveTranslation();

		if (Gdx.input.isKeyPressed(Keys.W)) {
			tmpVec.set(player.getDirection());
			moveTranslation.set(tmpVec.x, 0, tmpVec.z);
		} else if (Gdx.input.isKeyPressed(Keys.S))
			moveTranslation.z += moveBy;

		if (Gdx.input.isKeyPressed(Keys.A))
			moveTranslation.x -= moveBy;
		else if (Gdx.input.isKeyPressed(Keys.D))
			moveTranslation.x += moveBy;

		if (moveTranslation.isZero())
			return;

		moveTranslation.nor().scl(moveBy);
		player.applyMovement();
	}

	private void pollKeybinds() {
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
	}

	@Override
	public void dispose() {
		sceneManager.dispose();
		environmentCubemap.dispose();
		diffuseCubemap.dispose();
		specularCubemap.dispose();
		skybox.dispose();
	}
}
