package hitonoriol.voxelsandbox.entity;

import java.util.function.Consumer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;

public class Creature extends Entity {

	private float speed = 5;
	private Vector3 moveTranslation = new Vector3();
	private Vector3 direction = new Vector3();

	public Creature(Model model) {
		super(model);
	}

	protected void setDirection(Vector3 direction) {
		this.direction = direction;
	}

	public Vector3 getDirection() {
		return direction;
	}

	public Vector3 getMoveTranslation() {
		return moveTranslation;
	}

	public Vector3 getXZMoveTranslation() {
		return moveTranslation.set(direction.x, 0, direction.z);
	}

	public void applyMovement() {
		getBody().translate(moveTranslation);
		moveTranslation.setZero();
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
}
