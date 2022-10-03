package hitonoriol.voxelsandbox.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.utils.Array;

import hitonoriol.voxelsandbox.entity.Entity;
import hitonoriol.voxelsandbox.entity.Player;
import hitonoriol.voxelsandbox.input.GameInput;
import hitonoriol.voxelsandbox.random.Random;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class World extends SceneManager {
	private final static int MAX_BONES = 128;

	private final Array<RenderableProvider> renderableProviders = super.getRenderableProviders();
	private Cubemap diffuseCubemap;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private SceneSkybox skybox;
	private DirectionalLightEx light = new DirectionalLightEx();

	private Player player = new Player();

	public World() {
		super(MAX_BONES);
		GameInput.register(player.getController());
		addEntity(player);
		setCamera(player.getCamera());
		initLight();
		setUpScene();
	}

	private void initLight() {
		light.direction.set(1, -3, 1).nor();
		light.color.set(Color.WHITE);
		environment.add(light);

		IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
		environmentCubemap = iblBuilder.buildEnvMap(1024);
		diffuseCubemap = iblBuilder.buildIrradianceMap(256);
		specularCubemap = iblBuilder.buildRadianceMap(10);
		iblBuilder.dispose();

		setAmbientLight(1f);
		Texture brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
		environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
		environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
		environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
		environment.set(new ColorAttribute(ColorAttribute.Fog, Color.WHITE));

		skybox = new SceneSkybox(environmentCubemap);
		setSkyBox(skybox);
	}

	private void setUpScene() {
		float d = 100f;
		for (int i = 0; i < 50; ++i)
			buildBox(Random.nextFloat(-d, d), 0f, Random.nextFloat(-d, d), Random.nextFloat(3, 20));
		buildBox(0, -5f, 0, 1000, 1, 1000);
	}

	private void buildBox(float x, float y, float z, float size) {
		buildBox(x, y, z, size, size, size);
	}
	
	private void buildBox(float x, float y, float z, float w, float h, float d) {
		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		Material material = new Material(PBRColorAttribute.createBaseColorFactor(Random.randomColor()));
		MeshPartBuilder partBuilder = builder.part(x + ", " + y + ", " + z, GL20.GL_TRIANGLES,
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, material);
		BoxShapeBuilder.build(partBuilder, x, y, z, w, h, d);
		addEntity(new Entity(builder.end()));
	}

	public void addEntity(Entity renderable) {
		renderableProviders.add(renderable);
	}

	@Override
	public void dispose() {
		environmentCubemap.dispose();
		diffuseCubemap.dispose();
		specularCubemap.dispose();
		skybox.dispose();
		super.dispose();
	}

	public Player getPlayer() {
		return player;
	}
}
