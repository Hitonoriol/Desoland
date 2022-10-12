package hitonoriol.voxelsandbox.input;

import static hitonoriol.voxelsandbox.VoxelSandbox.player;
import static hitonoriol.voxelsandbox.VoxelSandbox.world;

import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

import hitonoriol.voxelsandbox.assets.Prefs;
import hitonoriol.voxelsandbox.assets.ShapeBuilder;
import hitonoriol.voxelsandbox.entity.Entity;
import hitonoriol.voxelsandbox.entity.Player;
import hitonoriol.voxelsandbox.io.Out;
import hitonoriol.voxelsandbox.util.Utils;

public class PlayerController extends PollableInputAdapter {
	private PerspectiveCamera camera;
	private Player player;
	private float sensitivity = 0.2f;
	private int mouseX = Gdx.graphics.getWidth() / 2, mouseY = Gdx.graphics.getHeight() / 2;
	private Vector3 tmpVec = new Vector3();

	private final static float MAX_REACH = 50f;
	private Vector3 rayFromPos = new Vector3();
	private Vector3 rayToPos = new Vector3();

	private final static Vector3 JUMP_IMPULSE = new Vector3(Vector3.Y).scl(20f);
	private final Vector3 groundDirection = new Vector3();

	private boolean ghostMode = false;

	private final static float SPRINT_FACTOR = 1.75f;
	private boolean sprinting = false;

	private final static float MAX_DELTA = 85f;
	private final static float VERTICAL_BOUND = 0.95f;

	public PlayerController(Player player) {
		this.camera = player.getCamera();
		this.player = player;
		Gdx.graphics.setSystemCursor(SystemCursor.Crosshair);
		Gdx.input.setCursorPosition(mouseX, mouseY);
		Gdx.input.setCursorCatched(true);
		groundDirection.y = -player.getHeight() * 0.5f;
		Out.print("Ground direction vector: %s", groundDirection);
		Out.print("Player center.y = %f, height = %f", player.getCenter().y, player.getHeight());
	}

	private void handleCameraRotation(int screenX, int screenY) {
		float dx = Utils.clamp(mouseX - screenX, MAX_DELTA);
		float dy = Utils.clamp(mouseY - screenY, MAX_DELTA);
		float rotX = Math.signum(dx) * Math.abs(dx) * sensitivity;
		float rotY = Math.signum(dy) * Math.abs(dy) * sensitivity;

		if (Gdx.input.isCursorCatched() && (rotX != 0 || rotY != 0)) {
			camera.rotate(Vector3.Y, rotX);

			if ((dy < 0 && camera.direction.y > -VERTICAL_BOUND)
					|| (dy > 0 && camera.direction.y < VERTICAL_BOUND))
				camera.rotate(tmpVec.set(camera.direction).crs(Vector3.Y), rotY);

			player.updateRotation();
		}

		mouseX = screenX;
		mouseY = screenY;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		handleCameraRotation(screenX, screenY);
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		handleCameraRotation(screenX, screenY);
		return false;
	}

	private Ray cameraRay() {
		return camera.getPickRay(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
	}

	private void raycast(Ray ray, Consumer<btCollisionObject> rayAction) {
		rayFromPos.set(ray.origin);
		rayToPos.set(ray.direction).scl(MAX_REACH).add(ray.origin);
		world().raycast(rayFromPos, rayToPos, rayAction);
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		switch (button) {
		case Buttons.LEFT: {
			Ray ray = cameraRay();
			raycast(ray, object -> {
				if (object instanceof btRigidBody obj) {
					obj.activate();
					obj.applyCentralImpulse(ray.direction.scl(350f));
				}
			});
			break;
		}
		case Buttons.RIGHT: {
			Ray ray = cameraRay();
			Entity projectile = new ShapeBuilder().sphere(1f);
			var projBody = projectile.getBody();
			var startPoint = player.getPosition().cpy().add(ray.direction.cpy().scl(1.5f));
			startPoint.y = player.getPosition().y + player.getHeight() * Prefs.values().firstPersonVerticalFactor;
			Out.print("Start: %s", startPoint);

			projBody.setIgnoreCollisionCheck(player().getBody(), true);
			projBody.setRestitution(0.25f);
			projectile.setMass(0.25f);
			projectile.placeAt(startPoint);
			projectile.syncBody();
			projBody.applyCentralImpulse(ray.direction.cpy().scl(30f));
			world().addEntity(projectile);
			break;
		}
		default:
			break;
		}
		return super.touchDown(screenX, screenY, pointer, button);
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.SPACE: {
			var body = player().getBody();
			var playerPos = player().getPosition();
			world().raycast(rayFromPos.set(playerPos),
					rayToPos.set(playerPos).add(groundDirection),
					object -> {
						if (object.checkCollideWith(body))
							body.applyCentralImpulse(JUMP_IMPULSE);
					});
			break;
		}

		case Keys.G: {
			player().getBody().setGravity(ghostMode ? world().getGravity() : Vector3.Zero);
			ghostMode ^= true;
			break;
		}

		case Keys.SHIFT_LEFT:
			sprinting = true;
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.SHIFT_LEFT:
			sprinting = false;
			break;

		default:
			break;
		}
		return false;
	}

	private static Vector3 UP = Vector3.Y.cpy().scl(0.75f);
	private static Vector3 DOWN = UP.cpy().scl(-1);

	@Override
	public void pollKeys() {
		if (ghostMode)
			player.getBody().setLinearVelocity(Vector3.Zero);

		Keyboard.getPressedKeys().forEach(this::processMovement);
		if (Gdx.input.isKeyPressed(Keys.R))
			impulseOrTranslate(UP);
		else if (Gdx.input.isKeyPressed(Keys.F))
			impulseOrTranslate(DOWN);
	}

	private void impulseOrTranslate(Vector3 moveBy) {
		var body = player.getBody();
		if (ghostMode)
			body.translate(moveBy);
		else
			body.applyCentralImpulse(moveBy);
	}

	private void processMovement(int key) {
		if (!Movement.isValidKey(key))
			return;

		float moveBy = player.getMovementSpeed() * Gdx.graphics.getDeltaTime();
		if (sprinting)
			moveBy *= SPRINT_FACTOR;

		var prefs = Prefs.values();
		camera.fieldOfView = sprinting ? prefs.cameraSprintingFov : prefs.cameraFov;

		Vector3 moveTranslation = player.getXZMoveTranslation();
		Movement.applyDirection(moveTranslation, key);

		if (moveTranslation.isZero())
			return;

		moveTranslation.nor().scl(moveBy);
		player.applyMovement();
	}

	public static class Movement {
		private static final Vector3 Forward = new Vector3(1, 0, 1),
				Backward = new Vector3(-1, 0, -1);

		public static boolean isValidKey(int key) {
			switch (key) {
			case Keys.W:
			case Keys.A:
			case Keys.S:
			case Keys.D:
				return true;
			default:
				return false;
			}
		}

		public static void applyDirection(Vector3 direction, int key) {
			if (key == Keys.W)
				direction.scl(Forward);
			else if (key == Keys.S)
				direction.scl(Backward);

			if (key == Keys.A)
				direction.crs(Vector3.Y).scl(-1f);
			else if (key == Keys.D)
				direction.crs(Vector3.Y);
		}
	}
}
