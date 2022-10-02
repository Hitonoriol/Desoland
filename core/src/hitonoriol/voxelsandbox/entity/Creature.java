package hitonoriol.voxelsandbox.entity;

import java.util.function.Consumer;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Creature extends Scene {

	private float speed = 5;
	private Vector3 moveTranslation = new Vector3();
	private Vector3 direction = new Vector3();
	private Vector3 position = new Vector3();
	private Quaternion rotation = new Quaternion();
	private Vector3 scale = new Vector3(1, 1, 1);

	public Creature(SceneAsset modelAsset) {
		super(modelAsset.scene);
	}

	protected void setDirection(Vector3 direction) {
		this.direction = direction;
	}

	public Vector3 getDirection() {
		return direction;
	}

	/* Extract position from world transform matrix & cache it */
	public Vector3 getPosition() {
		return position;
	}

	public Quaternion getRotation() {
		return rotation;
	}

	public void rotate(Vector3 axis, Vector3 direction) {
		rotation.setFromCross(axis, direction);
	}

	public Vector3 getMoveTranslation() {
		return moveTranslation;
	}
	
	public Vector3 getXZMoveTranslation() {
		return moveTranslation.set(direction.x, 0, direction.z);
	}
	
	protected void positionModified() {
		modelInstance.transform.getTranslation(position);
	}
	
	public void applyMovement() {
		applyTransform();
		modelInstance.transform.trn(moveTranslation);
		moveTranslation.setZero();
		positionModified();
	}

	public void move(Consumer<Vector3> moveTranslationSetter) {
		moveTranslationSetter.accept(moveTranslation);
	}

	public void setMovementSpeed(float speed) {
		this.speed = speed;
	}

	public float getMovementSpeed() {
		return speed;
	}

	public void applyTransform() {
		modelInstance.transform.set(position, rotation, scale);
	}
}
