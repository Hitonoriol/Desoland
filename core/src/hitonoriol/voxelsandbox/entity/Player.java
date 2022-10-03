package hitonoriol.voxelsandbox.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

import hitonoriol.voxelsandbox.assets.Models;
import hitonoriol.voxelsandbox.assets.Prefs;
import hitonoriol.voxelsandbox.input.PlayerController;

public class Player extends Creature {
	private PerspectiveCamera camera = new PerspectiveCamera(75f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	private PlayerController controller = new PlayerController(this);
	private Vector3 tmpVec = new Vector3();

	public Player() {
		super(Models.player);
		initBody(info -> {
			info.setMass(0);
			info.setLinearSleepingThreshold(0);
			info.setAngularSleepingThreshold(0);
		});
		initCamera();
		setMovementSpeed(10);
	}

	private void initCamera() {
		camera.near = 0.01f;
		camera.far = 1000;
		setDirection(camera.direction);
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

	private void updateCamera() {
		camera.position.set(getPOV());
		camera.update();
	}

	private Vector3 getPOV() {
		var prefs = Prefs.values();
		if (prefs.firstPersonCamera)
			return tmpVec.set(getDirection()).nor().scl(prefs.firstPersonHorizontalDistance)
					.add(getPosition())
					.set(tmpVec.x,
							getHeight() * prefs.firstPersonVerticalFactor,
							tmpVec.z);
		else {
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
		syncBody();
		updateCamera();
	}

	@Override
	public void applyTransform() {
		super.applyTransform();
		syncBody();
		updateCamera();
	}
}
