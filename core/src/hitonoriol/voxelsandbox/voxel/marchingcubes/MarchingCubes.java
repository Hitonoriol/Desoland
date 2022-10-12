package hitonoriol.voxelsandbox.voxel.marchingcubes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.badlogic.gdx.math.Vector3;

import hitonoriol.voxelsandbox.io.Out;
import hitonoriol.voxelsandbox.util.Utils;
import hitonoriol.voxelsandbox.voxel.Chunk;

/**
 * Created by Primoz on 11.07.2016.
 * Improved (?) game-friendly version by Hitonoriol.
 */
public class MarchingCubes {
	private Chunk chunk; // Chunk being polygonized
	/* Copies of chunk properties */
	private int horizontalSize, height;

	/* Affects the scale of individual segments the mesh consists of, doesn't change the mesh's dimensions.
	 * Values less than 1 will cause fixed size gaps to appear between segments. 
	 * Should be in range (0; 1] */
	private float segmentScale = 1f;
	private int cubeSize = 1; // Size of the marching cube (in voxels)

	private short indices[];

	public MarchingCubes(Chunk chunk) {
		horizontalSize = chunk.getHorizontalSize();
		height = chunk.getHeight();
		this.chunk = chunk;
	}

	public float[] createVertices() {
		List<Vector3> vertices = new ArrayList<Vector3>();
		Vector3 position = new Vector3();

		for (int z = 0; z < horizontalSize - 1; ++z) {
			for (int y = 0; y < height - 1; ++y) {
				for (int x = 0; x < horizontalSize - 1; ++x) {
					int cubeIndex = 0;
					if (isActive(x, y, z))
						cubeIndex |= 16;
					if (isActive(x + cubeSize, y, z))
						cubeIndex |= 32;
					if (isActive(x + cubeSize, y, z + cubeSize))
						cubeIndex |= 64;
					if (isActive(x, y, z + cubeSize))
						cubeIndex |= 128;
					if (isActive(x, y + cubeSize, z))
						cubeIndex |= 1;
					if (isActive(x + cubeSize, y + cubeSize, z))
						cubeIndex |= 2;
					if (isActive(x + cubeSize, y + cubeSize, z + cubeSize))
						cubeIndex |= 4;
					if (isActive(x, y + cubeSize, z + cubeSize))
						cubeIndex |= 8;

					int edgeVal = MarchingCubesTable.EDGE_TABLE[cubeIndex];
					if (edgeVal == 0)
						continue;

					position.set(x, y, z);
					Vector3 verts[] = calculateTriangleTranslations(edgeVal, x, y, z);

					int[] tris = MarchingCubesTable.TRI_TABLE[cubeIndex];
					for (int i = 0; tris[i] != -1; i += 3) {
						vertices.add(verts[tris[i]]);
						vertices.add(verts[tris[i + 1]]);
						vertices.add(verts[tris[i + 2]]);
					}
				}
			}
		}

		/* Lazy indexing without vertex de-duplication */
		indices = new short[vertices.size()];
		for (int i = 0; i < vertices.size(); ++i)
			indices[i] = (short) i;

		return generateVertexArray(vertices);
	}
	
	public short[] getIndices() {
		return indices;
	}

	private Vector3[] calculateTriangleTranslations(int edgeVal, int x, int y, int z) {
		Vector3 vertlist[] = new Vector3[12];
		if ((edgeVal & 1) > 0)
			vertlist[0] = new Vector3(x + segmentScale / 2f, y + segmentScale, z);
		if ((edgeVal & 2) > 0)
			vertlist[1] = new Vector3(x + segmentScale, y + segmentScale, z + segmentScale / 2f);
		if ((edgeVal & 4) > 0)
			vertlist[2] = new Vector3(x + segmentScale / 2f, y + segmentScale, z + segmentScale);
		if ((edgeVal & 8) > 0)
			vertlist[3] = new Vector3(x, y + segmentScale, z + segmentScale / 2f);

		if ((edgeVal & 16) > 0)
			vertlist[4] = new Vector3(x + segmentScale / 2f, y, z);
		if ((edgeVal & 32) > 0)
			vertlist[5] = new Vector3(x + segmentScale, y, z + segmentScale / 2f);
		if ((edgeVal & 64) > 0)
			vertlist[6] = new Vector3(x + segmentScale / 2f, y, z + segmentScale);
		if ((edgeVal & 128) > 0)
			vertlist[7] = new Vector3(x, y, z + segmentScale / 2f);

		if ((edgeVal & 256) > 0)
			vertlist[8] = new Vector3(x, y + segmentScale / 2f, z);
		if ((edgeVal & 512) > 0)
			vertlist[9] = new Vector3(x + segmentScale, y + segmentScale / 2f, z);
		if ((edgeVal & 1024) > 0)
			vertlist[10] = new Vector3(x + segmentScale, y + segmentScale / 2f, z + segmentScale);
		if ((edgeVal & 2048) > 0)
			vertlist[11] = new Vector3(x, y + segmentScale / 2f, z + segmentScale);
		return vertlist;
	}

	private float[] generateVertexArray(List<Vector3> vertices) {
		int numVertices = vertices.size();
		/* Vertices + normals */
		float vertArr[] = new float[numVertices * Chunk.VERTEX_ARR_COMPONENTS];
		{
			Vector3 faceNormal = new Vector3(), tmp = new Vector3();
			for (int v = 0, i = 0; v < vertices.size(); ++v) {
				var n = vertices.get(v);
				vertArr[i++] = n.x * Chunk.HORIZONTAL_SCALE;
				vertArr[i++] = (n.y - height) * Chunk.VERTICAL_SCALE;
				vertArr[i++] = n.z * Chunk.HORIZONTAL_SCALE;
				i += 3;
				/* For each face */
				if (i % 9 == 0) {
					/* Calculate face normals: for face ABC -- (B - A) x (C - B) */
					var a = vertices.get(v - 2);
					var b = vertices.get(v - 1);
					var c = vertices.get(v);
					faceNormal.set(b).sub(a).crs(tmp.set(c).sub(b));
					//Out.print("Face normal #%d: (%s)", (v + 1) / 3, Utils.vectorString(faceNormal));

					/* Sum all face normals for vertices they're touching */
					addToVertexNormals(vertArr, vertices, a, b, c, faceNormal);
				}
			}
			/* Normalize summed face normals */
			finalizeVertexNormals(vertArr);
		}
		//dump(vertArr);
		return vertArr;
	}

	private void finalizeVertexNormals(float[] verts) {
		/* 0  1  2  3  4  5  6  7  8  9  10 11 */
		/* vx vy vz nx ny nz vx vy vz nx ny nz ... */
		Vector3 normal = new Vector3();
		for (int i = 3; i < verts.length; i += Chunk.VERTEX_ARR_COMPONENTS) {
			normal.set(verts[i], verts[i + 1], verts[i + 2]).nor();
			verts[i] = normal.x;
			verts[i + 1] = normal.y;
			verts[i + 2] = normal.z;
		}
	}

	private void addToVertexNormals(
			float[] verts, List<Vector3> vertList,
			Vector3 a, Vector3 b, Vector3 c,
			Vector3 faceNormal) {
		for (int i = 0; i < vertList.size(); ++i) {
			Vector3 vert = vertList.get(i);
			if (vert.equals(a) || vert.equals(b) || vert.equals(c)) {
				int norIdx = i * Chunk.VERTEX_ARR_COMPONENTS + 3;
				verts[norIdx] += faceNormal.x;
				verts[norIdx + 1] += faceNormal.y;
				verts[norIdx + 2] += faceNormal.z;
				//Out.print("Added to vertex normals @ %d", norIdx);
			}
		}
	}

	private boolean isActive(int x, int y, int z) {
		return !chunk.get(x, y, z).isAir();
	}

	private void dump(float[] v) {
		Out.print("Vertex array dump");
		for (int i = 0, vertex, triangle = 0; i < v.length; i += Chunk.VERTEX_ARR_COMPONENTS) {
			vertex = 1 + (i / Chunk.VERTEX_ARR_COMPONENTS);
			if (vertex % 3 == 1)
				Out.print("\n----------Triangle #%d----------", ++triangle);
			Out.print("%d [%d - %d]) V: %.2f, %.2f, %.2f; N = (%.2f, %.2f, %.2f)",
					vertex, i, i + 5,
					v[i], v[i + 1], v[i + 2],
					v[i + 3], v[i + 4], v[i + 5]);
		}
	}
}