package hitonoriol.voxelsandbox.entity;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import hitonoriol.voxelsandbox.io.Out;

public class Entity extends ModelInstance {
	private Vector3 position = new Vector3();
	private Quaternion rotation = new Quaternion();
	private Vector3 scale = new Vector3(1, 1, 1);

	private BoundingBox bounds = new BoundingBox();
	private Vector3 dimensions = new Vector3();
	private Vector3 center = new Vector3();

	public Entity(Model model) {
		super(model);
		calculateBoundingBox(bounds);
		bounds.getCenter(center);
		bounds.getDimensions(dimensions);
		Out.print("%s (%X): %s",
				getClass().getSimpleName(), hashCode(), bounds);
	}

	public Entity(ModelInstance modelInstance) {
		super(modelInstance);
	}

	public BoundingBox getBoundingBox() {
		return bounds;
	}

	public Vector3 getDimensions() {
		return dimensions;
	}

	public float getWidth() {
		return dimensions.x;
	}

	public float getHeight() {
		return dimensions.y;
	}

	public float getDepth() {
		return dimensions.z;
	}

	public Vector3 getCenter() {
		return center;
	}

	public void placeAt(Vector3 position) {
		this.position.set(position);
		applyTransform();
	}

	public Vector3 getPosition() {
		return position;
	}

	public Quaternion getRotation() {
		return rotation;
	}

	public void rotate(Vector3 axis, Vector3 direction) {
		rotation.setFromCross(axis, direction);
	}

	protected void scaleChanged() {
		transform.getScale(scale);
	}

	protected void rotationChanged() {
		transform.getRotation(rotation);
	}

	protected void positionChanged() {
		transform.getTranslation(position);
	}

	public void scale(float by) {
		scale.set(by, by, by);
	}

	public Vector3 getScale() {
		return scale;
	}

	public void applyTransform() {
		transform.set(position, rotation, scale);
	}
}
