package hitonoriol.voxelsandbox.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

import hitonoriol.voxelsandbox.assets.Models;
import hitonoriol.voxelsandbox.input.PlayerCameraController;

public class Player extends Creature {
	private PerspectiveCamera camera = new PerspectiveCamera(75f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	private PlayerCameraController cameraController = new PlayerCameraController(this);

	public Player() {
		super(Models.player);
		initCamera();
		setMovementSpeed(10);
	}
	
	private void initCamera() {
		Gdx.input.setInputProcessor(cameraController);
		camera.near = 0.01f;
		camera.far = 1000;
		setDirection(camera.direction);
		syncPositions();
	}

	public PerspectiveCamera getCamera() {
		return camera;
	}
	
	private void syncPositions() {
		camera.position.set(getPosition());
	}
	
	@Override
	public void applyMovement(Vector3 moveTranslation) {
		super.applyMovement(moveTranslation);
		camera.position.set(getPosition());
		camera.update();
	}
}
