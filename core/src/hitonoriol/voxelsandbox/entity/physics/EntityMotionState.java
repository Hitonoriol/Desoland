package hitonoriol.voxelsandbox.entity.physics;

import static hitonoriol.voxelsandbox.VoxelSandbox.world;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

import hitonoriol.voxelsandbox.entity.Entity;

public class EntityMotionState extends btMotionState {
	private static final float MIN_Y = -100f;
	private Entity entity;

	public EntityMotionState(Entity entity) {
		this.entity = entity;
	}

	@Override
	public void getWorldTransform(Matrix4 worldTrans) {
		worldTrans.set(entity.transform);
	}

	@Override
	public void setWorldTransform(Matrix4 worldTrans) {
		entity.setTransform(worldTrans);
		if (entity.getPosition().y < MIN_Y)
			world().destroyEntity(entity);
	}
}
