package hitonoriol.voxelsandbox.entity;

import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector3;

import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Creature extends Scene {

	private float speed = 5;
	private Vector3 moveTranslation = new Vector3();
	private Vector3 direction = new Vector3();
	private Vector3 position = new Vector3();

	public Creature(SceneAsset modelAsset) {
		super(modelAsset.scene);
	}

	protected void setDirection(Vector3 direction) {
		this.direction = direction;
	}
	
	public Vector3 getDirection() {
		return direction;
	}
	
	public Vector3 getPosition() {
		return modelInstance.transform.getTranslation(position);
	}

	public Vector3 getMoveTranslation() {
		return moveTranslation;
	}
	
	public void applyMovement(Vector3 moveTranslation) {
		modelInstance.transform.translate(moveTranslation);
		moveTranslation.set(0, 0, 0);
	}
	
	public void applyMovement() {
		applyMovement(this.moveTranslation);
	}
	
	public void move(Consumer<Vector3> moveTranslationSetter) {
		moveTranslationSetter.accept(moveTranslation);
		applyMovement();
	}
	
	public void setMovementSpeed(float speed) {
		this.speed = speed;
	}
	
	public float getMovementSpeed() {
		return speed;
	}
}
