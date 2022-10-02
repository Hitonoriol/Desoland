package hitonoriol.voxelsandbox.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

import hitonoriol.voxelsandbox.assets.Models;
import hitonoriol.voxelsandbox.input.PlayerController;

public class Player extends Creature {
	private PerspectiveCamera camera = new PerspectiveCamera(75f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	private PlayerController controller = new PlayerController(this);
	private Vector3 tmpVec = new Vector3();

	public Player() {
		super(Models.player);
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
		camera.position.set(getPosition());
		updateRotation();
	}
	
	public void updateRotation() {
		tmpVec.set(camera.direction.x, 0, camera.direction.z).nor();
		rotate(Vector3.X, tmpVec);
	}

	private void updateCamera() {
		camera.position.set(getPosition());
		camera.update();
	}

	@Override
	protected void positionModified() {
		super.positionModified();
		updateCamera();
	}

	@Override
	public void applyTransform() {
		super.applyTransform();
		updateCamera();
	}
}
