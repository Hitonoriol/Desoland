package hitonoriol.voxelsandbox.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import hitonoriol.voxelsandbox.entity.Player;

public class PlayerCameraController extends InputAdapter {
	private PerspectiveCamera camera;
	private Player player;
	private float sensitivity = 0.2f;
	private float mouseX = Gdx.graphics.getWidth() / 2, mouseY = Gdx.graphics.getHeight() / 2;
	private Vector3 tmpVec = new Vector3(), tmpVecB = new Vector3();
	private Matrix4 tmpMatrix = new Matrix4();

	private final static float VERTICAL_BOUND = 0.95f;

	public PlayerCameraController(Player player) {
		this.camera = player.getCamera();
		this.player = player;
		Gdx.graphics.setSystemCursor(SystemCursor.Crosshair);
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
				

			tmpVecB.setZero().set(camera.direction.x, player.getPosition().y, camera.direction.z);
			//player.modelInstance.transform.setToLookAt(tmpVecB, camera.up);
		}
		
		camera.update();
		mouseX = screenX;
		mouseY = screenY;
		return false;
	}
}
