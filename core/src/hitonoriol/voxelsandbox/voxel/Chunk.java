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
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;

import hitonoriol.voxelsandbox.assets.Materials;
import hitonoriol.voxelsandbox.io.Out;
import hitonoriol.voxelsandbox.random.Noise;
import hitonoriol.voxelsandbox.random.Random;
import hitonoriol.voxelsandbox.voxel.marchingcubes.MarchingCubes;

public class Chunk {
	private int horizontalSize, height;
	private final Voxel voxels[];
	private float groundLevel;
	private boolean ready = false;
	private boolean needsUpdate = false;

	public static final int VERTEX_ARR_COMPONENTS = 6;
	public static final float HORIZONTAL_SCALE = 10f, VERTICAL_SCALE = 10f;
	private final Matrix4 transform = new Matrix4();
	private final Vector3 worldPosition = new Vector3();

	private final MarchingCubes vertexFactory;
	private final NodePart nodePart = new NodePart(new MeshPart(), Materials.reflective(Random.randomColor()));

	private final Node node = new Node();
	private final btRigidBody body = new btRigidBody(new btRigidBodyConstructionInfo(0, null, null));

	public Chunk(int horizontalSize, int height) {
		this.horizontalSize = horizontalSize;
		this.height = height;
		voxels = new Voxel[horizontalSize * height * horizontalSize];
		vertexFactory = new MarchingCubes(this);
		node.parts.add(nodePart);
		nodePart.meshPart.offset = 0;
		nodePart.meshPart.primitiveType = GL20.GL_TRIANGLES;
		nodePart.meshPart.mesh = new Mesh(false,
				voxels.length * 6, voxels.length * 6,
				VertexAttribute.Position(),
				VertexAttribute.Normal());
		clear();
	}

	public void clear() {
		ready = false;
		forEachVoxel((x, y, z) -> set(new Voxel(), x, y, z));
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
		var scale = (horizontalSize - 1) * HORIZONTAL_SCALE;
		transform.setToTranslation(
				worldPosition.x * scale,
				(height) * VERTICAL_SCALE,
				worldPosition.z * scale);
		body.setWorldTransform(transform);
	}

	public Vector3 getWorldPosition() {
		return worldPosition;
	}

	public Matrix4 getTransform() {		
		return transform;
	}

	public boolean isValidVoxel(int x, int y, int z) {
		return (x >= 0 && x < horizontalSize)
				&& (y >= 0 && y < height)
				&& (z >= 0 && z < horizontalSize);
	}

	private int getIdx(final int x, final int y, final int z) {
		return height * horizontalSize * z + horizontalSize * y + x;
	}

	public Voxel get(int x, int y, int z) {
		if (!isValidVoxel(x, y, z))
			return Voxel.air;
		return voxels[getIdx(x, y, z)];
	}

	public void set(Voxel value, int x, int y, int z) {
		if (!isValidVoxel(x, y, z))
			Out.print("Trying to set a non-existing voxel: %d, %d, %d", x, y, z);
		voxels[getIdx(x, y, z)] = value;
	}

	public synchronized void generate(long seed) {
		ready = false;
		Out.print("Generating %s", this);
		Noise noiseGen = new Noise(seed);
		noiseGen.setHeight(height);
		groundLevel = 0.25f;
		for (int x = 0; x < horizontalSize; ++x) {
			for (int y = 0; y < height; ++y) {
				for (int z = 0; z < horizontalSize; ++z) {
					float value = -Math.abs(
							noiseGen.generateSimple(
									worldPosition.x * horizontalSize + x,
									y,
									worldPosition.z * horizontalSize + z) * height
					) + y;

					if (value >= groundLevel)
						get(x, y, z).setValue(value);
				}
			}
		}
		needsUpdate = true;
		Out.print("Finished generating %s", this);
	}

	public void forEachVoxel(PositionConsumer positionConsumer) {
		for (int y = 0; y < height; ++y) {
			for (int z = 0; z < horizontalSize; ++z) {
				for (int x = 0; x < horizontalSize; ++x) {
					positionConsumer.accept(x, y, z);
				}
			}
		}
	}

	public Voxel[] getData() {
		return voxels;
	}

	public MeshPart getMeshPart() {
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

	public float getGroundLevel() {
		return groundLevel;
	}

	void updateMesh() {
		Out.print("Updating mesh for chunk %s", this);
		var mesh = nodePart.meshPart.mesh;
		mesh.setVertices(vertexFactory.createVertices());
		mesh.setIndices(vertexFactory.getIndices());
		nodePart.meshPart.size = mesh.getNumIndices();
		nodePart.meshPart.update();
		body.setCollisionShape(Bullet.obtainStaticNodeShape(node, false));
		var verts = mesh.getNumVertices();
		var indices = mesh.getNumIndices();
		Out.print("Done! Built %d triangles (%d vertices / %d indices)",
				verts / 3, verts, indices);
		needsUpdate = false;
		ready = true;
	}
	
	void beginUpdate() {
		needsUpdate = false;
	}
	
	public boolean needsUpdate() {
		return needsUpdate;
	}

	@Override
	public String toString() {
		return String.format("Chunk @ %d, %d [%dx%dx%d]",
				(int) worldPosition.x, (int) worldPosition.z, horizontalSize, height, horizontalSize);
	}

	public static interface PositionConsumer {
		void accept(int x, int y, int z);
	}
}
