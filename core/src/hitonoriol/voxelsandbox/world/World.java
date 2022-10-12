package hitonoriol.voxelsandbox.world;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.RayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw.DebugDrawModes;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;

import hitonoriol.voxelsandbox.assets.Materials;
import hitonoriol.voxelsandbox.assets.Prefs;
import hitonoriol.voxelsandbox.assets.ShapeBuilder;
import hitonoriol.voxelsandbox.entity.Entity;
import hitonoriol.voxelsandbox.entity.Player;
import hitonoriol.voxelsandbox.input.GameInput;
import hitonoriol.voxelsandbox.random.Random;
import hitonoriol.voxelsandbox.util.async.Async;
import hitonoriol.voxelsandbox.voxel.Terrain;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class World extends SceneManager {
	private final static int MAX_BONES = 128;

	private final Array<RenderableProvider> renderableProviders = super.getRenderableProviders();
	private final List<Entity> entities = new ArrayList<>();

	private Cubemap diffuseCubemap;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private SceneSkybox skybox;
	private DirectionalLightEx light = new DirectionalShadowLight();

	private static final int MAX_SUBSTEPS = 500;
	private btCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
	private btDispatcher collisionDispatcher = new btCollisionDispatcher(collisionConfiguration);
	private btBroadphaseInterface broadphase = new btDbvtBroadphase();
	private btConstraintSolver constraintSolver = new btSequentialImpulseConstraintSolver();
	private btDynamicsWorld dynamicsWorld = new btDiscreteDynamicsWorld(
			collisionDispatcher, broadphase, constraintSolver, collisionConfiguration);

	private final Vector3 gravity = new Vector3(0, -40, 0);

	private Player player = new Player();
	private Terrain terrain = new Terrain(this, 5);

	private DebugDrawer debugDrawer = new DebugDrawer();
	private ShapeBuilder shapeBuilder = new ShapeBuilder();

	public World() {
		super(MAX_BONES);
		GameInput.register(player.getController());
		player.placeAt(terrain.getVisualCenter(), Terrain.CHUNK_VISUAL_HEIGHT, terrain.getVisualCenter());
		player.syncBody();
		addEntity(player);
		setCamera(player.getCamera());
		initLight();
		initPhysics();
		setUpScene();
		terrain.generate();
		addRenderable(terrain);
	}

	private void initLight() {
		light.intensity = 2.5f;
		light.direction.set(1, -3, 1).nor();
		light.color.set(Color.WHITE);
		environment.add(light);

		IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
		environmentCubemap = iblBuilder.buildEnvMap(1024);
		diffuseCubemap = iblBuilder.buildIrradianceMap(512);
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

	private void initPhysics() {
		physicsPropertiesChanged();
		dynamicsWorld.setDebugDrawer(debugDrawer);
		debugDrawer.setDebugMode(Prefs.debug ? DebugDrawModes.DBG_DrawWireframe : DebugDrawModes.DBG_NoDebug);
	}

	private void setUpScene() {
		CompletableFuture.runAsync(() -> {
			for (int i = 0; i < 200; ++i)
				Gdx.app.postRunnable(() -> {
					spawnRandomShape();
				});
		}, Async.executor());

		/* addGround(terrain.getVisualCenter(), terrain.getVisualCenter(),
				terrain.getVisualSize(), terrain.getVisualSize()); */

		Timer.instance().scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				if (dynamicsWorld.getNumCollisionObjects() < 100) {
					spawnRandomShape();
				}
			}
		}, 0f, 1.5f);
	}

	private void addGround(float x, float z, float w, float d) {
		Entity ground = shapeBuilder.box(w, 0.5f, d);
		ground.placeAt(x, -1f, z);
		ground.syncBody();
		dynamicsWorld.addRigidBody(ground.getBody());
		//placeEntity(ground, x, -0.5f, z);
	}

	private void spawnRandomShape() {
		float minSize = 1, maxSize = 20;
		Entity shape = shapeBuilder.random(Random.nextFloat(minSize, maxSize));
		shape.setMass(Random.nextFloat(0.1f, 5f));
		shape.rotate(Vector3.Z, Random.nextFloat(0, 270f));
		placeEntity(shape);

		var body = shape.getBody();
		body.setRestitution(Random.nextFloat(0, 0.75f));
		body.applyCentralImpulse(Random.vector3().scl(Random.nextFloat(1, 35)));
		body.setGravity(Vector3.Zero);
	}

	private Entity placeEntity(Entity entity) {
		var range = terrain.getVisualSize();
		return placeEntity(entity,
				Random.nextFloat(-range, range),
				Random.nextFloat(Terrain.CHUNK_VISUAL_HEIGHT * 0.5f, Terrain.CHUNK_VISUAL_HEIGHT * 1.5f),
				Random.nextFloat(-range, range));
	}

	private Entity placeEntity(Entity entity, float x, float y, float z) {
		entity.placeAt(x, y, z);
		entity.syncBody();
		addEntity(entity);
		return entity;
	}

	public void physicsPropertiesChanged() {
		dynamicsWorld.setGravity(gravity);
	}

	public void addRenderable(RenderableProvider renderable) {
		renderableProviders.add(renderable);
	}

	public void removeRenderable(RenderableProvider renderable) {
		renderableProviders.removeValue(renderable, true);
	}

	public synchronized void addEntity(Entity entity) {
		entity.init(this);
		addRenderable(entity);
		entities.add(entity);
		if (entity.hasBody())
			dynamicsWorld.addRigidBody(entity.getBody());
	}

	private synchronized void removeEntity(Entity entity) {
		removeRenderable(entity);
		entities.remove(entity);
		if (entity.hasBody())
			dynamicsWorld.removeRigidBody(entity.getBody());
	}

	public void destroyEntity(Entity entity) {
		entity.destroy();
		removeEntity(entity);
		entity.dispose();
	}

	@Override
	public void update(float delta) {
		dynamicsWorld.stepSimulation(delta, MAX_SUBSTEPS);
		super.update(delta);
	}

	public void debugRender(Camera camera) {
		debugDrawer.begin(camera);
		dynamicsWorld.debugDrawWorld();
		debugDrawer.end();
	}

	public void raycast(Vector3 from, Vector3 to, Consumer<btCollisionObject> rayAction) {
		var callback = new ClosestRayResultCallback(from, to);
		raycast(from, to, callback);
		if (callback.hasHit())
			rayAction.accept(callback.getCollisionObject());
	}

	public void raycast(Vector3 from, Vector3 to, RayResultCallback callback) {
		dynamicsWorld.rayTest(from, to, callback);
	}

	public Vector3 getGravity() {
		return gravity;
	}

	public btCollisionConfiguration getCollisionConfiguration() {
		return collisionConfiguration;
	}

	public btDispatcher getCollisionDispatcher() {
		return collisionDispatcher;
	}

	public btDynamicsWorld getDynamicsWorld() {
		return dynamicsWorld;
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
