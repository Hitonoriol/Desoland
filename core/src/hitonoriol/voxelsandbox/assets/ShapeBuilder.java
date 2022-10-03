package hitonoriol.voxelsandbox.assets;

import java.util.function.Function;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;

import hitonoriol.voxelsandbox.entity.Entity;
import hitonoriol.voxelsandbox.random.Random;

public class ShapeBuilder {
	private ModelBuilder builder = new ModelBuilder();

	public Entity box(float size) {
		return box(size, size, size);
	}

	public Entity box(float w, float h, float d) {
		Entity box = build(() -> BoxShapeBuilder.build(coloredPartBuilder(Random.randomColor()), w, h, d));
		box.initBody();
		return box;
	}

	public Entity sphere(float d) {
		Entity sphere = build(
				() -> SphereShapeBuilder.build(coloredPartBuilder(Random.randomColor()), d, d, d, 25, 25));
		sphere.initBody(info -> info.setCollisionShape(new btSphereShape(d / 2f)));
		return sphere;
	}

	public Entity cone(float size) {
		Entity cone = build(
				() -> ConeShapeBuilder.build(coloredPartBuilder(Random.randomColor()), size, size, size, 25));
		cone.initBody(info -> info.setCollisionShape(new btConeShape(size / 2f, size)));
		return cone;
	}

	public Entity cylinder(float size) {
		Entity cylinder = build(
				() -> CylinderShapeBuilder.build(coloredPartBuilder(Random.randomColor()), size, size, size, 25));
		cylinder.initBody(info -> info.setCollisionShape(new btCylinderShape(new Vector3(size, size, size).scl(0.5f))));
		return cylinder;
	}

	public Entity random(float size) {
		return pick(
				this::box,
				this::sphere,
				this::cone,
				this::cylinder
		).apply(size);
	}

	private Entity build(Runnable buildAction) {
		builder.begin();
		buildAction.run();
		return new Entity(builder.end());
	}

	private MeshPartBuilder coloredPartBuilder(Color color) {
		return builder.part("shape",
				GL20.GL_TRIANGLES,
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
				Materials.colored(color));
	}

	@SafeVarargs
	public final Function<Float, Entity> pick(Function<Float, Entity>... builders) {
		return builders[Random.get().nextInt(0, builders.length - 1)];
	}
}
