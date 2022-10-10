package hitonoriol.voxelsandbox.voxel;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;

import hitonoriol.voxelsandbox.assets.Materials;
import hitonoriol.voxelsandbox.io.Out;
import hitonoriol.voxelsandbox.random.OpenSimplex2;
import hitonoriol.voxelsandbox.random.Random;
import hitonoriol.voxelsandbox.voxel.marchingcubes.MarchingCubes;

public class Chunk {
	private int horizontalSize, height;
	private float voxels[];
	private boolean ready = false;
	private boolean needsUpdate = true;

	public static final float HORIZONTAL_SCALE = 6f, VERTICAL_SCALE = 3.5f;
	private Matrix4 transform = new Matrix4();
	private Vector3 worldPosition = new Vector3();

	private VertexFactory vertexFactory;
	private NodePart nodePart = new NodePart(new MeshPart(), Materials.colored(Random.randomColor()));

	private Node node = new Node();
	private btRigidBody body = new btRigidBody(new btRigidBodyConstructionInfo(0, null, null));

	public Chunk(int horizontalSize, int height) {
		this.horizontalSize = horizontalSize;
		this.height = height;
		voxels = new float[horizontalSize * height * horizontalSize];
		vertexFactory = new MarchingCubes(this);
		node.parts.add(nodePart);
		nodePart.meshPart.offset = 0;
		nodePart.meshPart.primitiveType = GL20.GL_TRIANGLES;
		nodePart.meshPart.mesh = new Mesh(false,
				voxels.length * 24, voxels.length * 24,
				VertexAttribute.Position(),
				VertexAttribute.Normal());
	}

	public int getHorizontalSize() {
		return horizontalSize;
	}

	public int getHeight() {
		return height;
	}

	public void setWorldPosition(int x, int z) {
		worldPosition.x = x;
		worldPosition.z = z;
	}

	public Vector3 getWorldPosition() {
		return worldPosition;
	}

	public Matrix4 getTransform() {
		var scale = (horizontalSize - 1) * HORIZONTAL_SCALE;
		transform.setToTranslation(
				worldPosition.x * scale,
				worldPosition.y,
				worldPosition.z * scale);
		return transform;
	}

	public boolean isValidVoxel(int x, int y, int z) {
		return (x >= 0 && x < horizontalSize)
				&& (y >= 0 && y < height)
				&& (z >= 0 && z < horizontalSize);
	}

	private int getIdx(final int x, final int y, final int z) {
		return x + z * horizontalSize + y * horizontalSize * height;
	}

	public float get(int x, int y, int z) {
		if (!isValidVoxel(x, y, z))
			return 0;
		return voxels[getIdx(x, y, z)];
	}

	public void set(float value, int x, int y, int z) {
		voxels[getIdx(x, y, z)] = value;
	}

	public void generate(long seed) {
		ready = false;
		Out.print("Generating chunk @ %s", getWorldPosition());
		float val, min = voxels[0], max = voxels[0];
		for (int y = 0; y < height; ++y) {
			for (int z = 0; z < horizontalSize; ++z) {
				for (int x = 0; x < horizontalSize; ++x) {
					val = OpenSimplex2.noise3_ImproveXZ(seed,
							worldPosition.x * horizontalSize + x, y, worldPosition.z * horizontalSize + z);
					set(val, x, y, z);
					min = Math.min(min, val);
					max = Math.max(max, val);
				}
			}
		}
		Out.print("Finished generating. [Min voxel: %f, max voxel: %f]", min, max);
		updateMesh();
		ready = true;
	}

	public MeshPart getMeshPart() {
		if (needsUpdate)
			updateMesh();
		return nodePart.meshPart;
	}

	public Material getMaterial() {
		return nodePart.material;
	}

	public btRigidBody getBody() {
		return body;
	}
	
	public boolean isReady() {
		return ready;
	}

	private void updateMesh() {
		Out.print("Updating mesh for chunk @ %s", getWorldPosition());
		var mesh = nodePart.meshPart.mesh;
		mesh.setVertices(vertexFactory.createVertices());
		nodePart.meshPart.size = mesh.getNumVertices();
		nodePart.meshPart.update();
		//body.setCollisionShape(Bullet.obtainStaticNodeShape(node, false));
		Out.print("Done! Built %d triangles", mesh.getNumVertices() / 6);
		needsUpdate = false;
	}
}
