package hitonoriol.voxelsandbox.entity;

import java.util.function.Consumer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;
import com.badlogic.gdx.utils.Disposable;

import hitonoriol.voxelsandbox.entity.physics.EntityMotionState;
import hitonoriol.voxelsandbox.io.Out;
import hitonoriol.voxelsandbox.util.Utils;

public class Entity extends ModelInstance implements Disposable {
	private Vector3 position = new Vector3();
	private Quaternion rotation = new Quaternion();
	private Vector3 scale = new Vector3(1, 1, 1);

	private BoundingBox bounds = new BoundingBox();
	private Vector3 dimensions = new Vector3();
	private Vector3 center = new Vector3();

	private btRigidBody rigidBody;
	private EntityMotionState motionState = new EntityMotionState(this);

	private boolean dead = false;

	public Entity(Model model) {
		super(model);
		calculateBoundingBox(bounds);
		bounds.getCenter(center);
		bounds.getDimensions(dimensions);
		Out.print("%s: %s",
				getDebugName(), bounds);
	}

	public Entity(ModelInstance modelInstance) {
		super(modelInstance);
	}

	/* Construct a btRigidBodyConstructionInfo with some default values and pass it to `initializer` for customization */
	public void initBody(Consumer<btRigidBodyConstructionInfo> initializer, Vector3 localInertia) {
		if (hasBody())
			return;

		var info = new btRigidBodyConstructionInfo(0f, motionState, null, Vector3.Zero);
		if (initializer != null)
			initializer.accept(info);

		/* Set default collision shape to model's bounding box */
		btCollisionShape shape = info.getCollisionShape();
		if (shape == null)
			info.setCollisionShape(shape = createDefaultCollisionShape());

		if (localInertia == null) {
			localInertia = new Vector3();
			shape.calculateLocalInertia(info.getMass(), localInertia);
		}
		info.setLocalInertia(new btVector3(localInertia.x, localInertia.y, localInertia.z));
		applyTransform();
		rigidBody = new btRigidBody(info);
	}

	public void initBody(Consumer<btRigidBodyConstructionInfo> initializer) {
		initBody(initializer, null);
	}

	public void setCollisionShape(btCollisionShape shape) {
		rigidBody.setCollisionShape(shape);
	}

	public void initBody() {
		initBody(null);
	}

	public void syncBody() {
		if (rigidBody != null) {
			rigidBody.proceedToTransform(transform);
		}
	}

	protected btCollisionShape createDefaultCollisionShape() {
		return new btBoxShape(dimensions.cpy().scl(0.5f));
	}

	public void setMass(float mass) {
		Vector3 inertia = new Vector3();
		rigidBody.getCollisionShape().calculateLocalInertia(mass, inertia);
		rigidBody.setMassProps(mass, inertia);
		rigidBody.activate();
	}

	public boolean hasBody() {
		return rigidBody != null;
	}

	public btRigidBody getBody() {
		return rigidBody;
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

	public boolean isStatic() {
		return rigidBody.getInvMass() == 0;
	}
	
	public boolean isDestroyed() {
		return dead;
	}

	public void destroy() {
		dead = true;
		Out.print("Destroying %s", getDebugName());
	}

	public void placeAt(float x, float y, float z) {
		position.set(x, y, z);
		applyTransform();
	}

	public void placeAt(Vector3 position) {
		placeAt(position.x, position.y, position.z);
	}

	public void setTransform(Matrix4 transform) {
		this.transform.set(transform);
		scaleChanged();
		rotationChanged();
		positionChanged();
	}

	public Vector3 getPosition() {
		return position;
	}

	public Quaternion getRotation() {
		return rotation;
	}

	public void rotate(Vector3 axis, Vector3 direction) {
		rotation.setFromCross(axis, direction);
		if (hasBody()) {
			rigidBody.setCenterOfMassTransform(transform.set(position, rotation, scale));
		}
	}

	public void rotate(Vector3 axis, float angle) {
		rotation.set(axis, angle);
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

	protected void applyTransform() {
		transform.set(position, rotation, scale);
	}

	@Override
	public void dispose() {
		rigidBody.dispose();
		motionState.dispose();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Entity entity)
			return position.equals(entity.position);
		return false;
	}
	
	private String getDebugName() {
		return String.format("%s (%X, dimensions: %s)",
				getClass().getSimpleName(), hashCode(), Utils.vectorString(dimensions));
	}
}
