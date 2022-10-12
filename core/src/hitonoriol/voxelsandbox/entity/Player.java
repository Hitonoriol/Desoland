package hitonoriol.voxelsandbox.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

import hitonoriol.voxelsandbox.VoxelSandbox;
import hitonoriol.voxelsandbox.assets.Models;
import hitonoriol.voxelsandbox.assets.Prefs;
import hitonoriol.voxelsandbox.input.GameInput;
import hitonoriol.voxelsandbox.input.PlayerController;
import hitonoriol.voxelsandbox.io.Out;
import hitonoriol.voxelsandbox.world.World;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.PointLightEx;

public class Player extends Creature {
	private PerspectiveCamera camera = new PerspectiveCamera(Prefs.values().cameraFov,
			Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	private PlayerController controller = new PlayerController(this);
	private Vector3 tmpVec = new Vector3();
	private PointLightEx light = new PointLightEx();

	public Player() {
		super(Models.player);
		initBody(info -> {
			info.setMass(0.75f);
			info.setFriction(0.95f);
		}, Vector3.Zero);
		setMovementSpeed(12);
		var body = getBody();
		body.setAngularFactor(Vector3.Y);
		body.setActivationState(Collision.DISABLE_DEACTIVATION);
		light.intensity = 25f;
		light.range = 200f;
		light.setColor(Color.RED);
		VoxelSandbox.deferInit(this::initCamera);
	}

	@Override
	public void init(World world) {
		world.environment.add(light);
	}

	@Override
	protected btCollisionShape createDefaultCollisionShape() {
		return new btCapsuleShape(getDepth() * 0.25f, getHeight() * 0.45f);
	}

	public void setViewDistance(float distance) {
		camera.far = distance + 25f;
		VoxelSandbox.world().environment
				.set(new FogAttribute(FogAttribute.FogEquation).set(camera.near, distance, 1f));
	}

	private void initCamera() {
		camera.near = 0.01f;
		setDirection(camera.direction);
		setViewDistance(Prefs.values().cameraViewDistance);
		syncCamera();
	}

	public PlayerController getController() {
		return controller;
	}

	public PerspectiveCamera getCamera() {
		return camera;
	}

	private void syncCamera() {
		updateCamera();
		updateRotation();
	}

	public void updateRotation() {
		tmpVec.set(camera.direction.x, 0, camera.direction.z).nor();
		rotate(Vector3.X, tmpVec);
	}

	@Override
	protected void rotationChanged() {}

	public void updateCamera() {
		camera.position.set(getPOV());
		camera.update();
	}

	private Vector3 getPOV() {
		var prefs = Prefs.values();
		if (prefs.firstPersonCamera) {
			tmpVec.set(getDirection()).nor().scl(prefs.firstPersonHorizontalDistance);
			tmpVec.y = 0;
			tmpVec.add(getPosition());
			tmpVec.y += getHeight() * prefs.firstPersonVerticalFactor;
			return tmpVec;
		} else {
			var dirVector = getDirection();
			var distance = prefs.thirdPersonHorizontalDistance;
			return tmpVec.set(getPosition())
					.add(-dirVector.x * distance,
							getHeight() + prefs.thirdPersonVerticalDistance,
							-dirVector.z * distance);
		}
	}

	@Override
	protected void positionChanged() {
		super.positionChanged();
		light.position.set(getDirection()).scl(2f);
		light.position.y = getHeight() * Prefs.values().firstPersonVerticalFactor;
		light.position.add(getPosition());
		updateCamera();
	}

	@Override
	public void destroy() {
		GameInput.unregister(controller);
		super.destroy();
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
