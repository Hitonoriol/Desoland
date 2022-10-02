package hitonoriol.voxelsandbox.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

import hitonoriol.voxelsandbox.entity.Player;

public class PlayerController extends InputAdapter {
	private PerspectiveCamera camera;
	private Player player;
	private float sensitivity = 0.2f;
	private int mouseX = Gdx.graphics.getWidth() / 2, mouseY = Gdx.graphics.getHeight() / 2;
	private Vector3 tmpVec = new Vector3(), tmpVecB = new Vector3();

	private final static float VERTICAL_BOUND = 0.95f;

	public PlayerController(Player player) {
		this.camera = player.getCamera();
		this.player = player;
		Gdx.graphics.setSystemCursor(SystemCursor.Crosshair);
		Gdx.input.setCursorPosition(mouseX, mouseY);
		Gdx.input.setCursorCatched(true);
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		float dx = mouseX - screenX, dy = mouseY - screenY;
		float rotX = Math.signum(dx) * Math.abs(dx) * sensitivity;
		float rotY = Math.signum(dy) * Math.abs(dy) * sensitivity;

		if (rotX != 0 || rotY != 0) {
			camera.rotate(Vector3.Y, rotX);

			if ((dy < 0 && camera.direction.y > -VERTICAL_BOUND)
					|| (dy > 0 && camera.direction.y < VERTICAL_BOUND))
				camera.rotate(tmpVec.set(camera.direction).crs(Vector3.Y), rotY);

			player.updateRotation();
		}

		player.applyTransform();
		mouseX = screenX;
		mouseY = screenY;
		return false;
	}

	public void pollKeys() {
		Keyboard.getPressedKeys().forEach(this::processMovement);
	}

	private void processMovement(int key) {
		if (!Movement.isValidKey(key))
			return;

		float moveBy = player.getMovementSpeed() * Gdx.graphics.getDeltaTime();
		Vector3 moveTranslation = player.getXZMoveTranslation();

		Movement.applyDirection(moveTranslation, key);

		if (moveTranslation.isZero())
			return;

		moveTranslation.nor().scl(moveBy);
		player.applyMovement();
	}
	
	public static class Movement {
		private static final Vector3
				Forward = new Vector3(1, 0, 1),
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
