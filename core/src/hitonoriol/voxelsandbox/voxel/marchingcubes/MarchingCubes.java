package hitonoriol.voxelsandbox.voxel.marchingcubes;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector3;

import hitonoriol.voxelsandbox.io.Out;
import hitonoriol.voxelsandbox.voxel.Chunk;
import hitonoriol.voxelsandbox.voxel.VertexFactory;

/**
 * Created by Primoz on 11.07.2016.
 * Vertex normals calculation by Hitonoriol.
 */
public class MarchingCubes extends VertexFactory {
	private Chunk chunk;
	private int horizontalSize, height;
	private float isoLevel = 0f;
	private Vector3 offset;

	public MarchingCubes(Chunk chunk) {
		horizontalSize = chunk.getHorizontalSize();
		height = chunk.getHeight();
		this.chunk = chunk;
		offset = new Vector3();
		offset.y = -height;
	}

	@Override
	public float[] createVertices() {
		List<Vector3> vertices = new ArrayList<Vector3>();

		for (int y = 0; y < height; y++) {
			for (int z = 0; z < horizontalSize; z++) {
				for (int x = 0; x < horizontalSize; x++) {
					int cubeIndex = 0;
					if (isValidVoxel(x, y, z))
						cubeIndex |= 16;
					if (isValidVoxel(x + 1, y, z))
						cubeIndex |= 32;
					if (isValidVoxel(x, y + 1, z))
						cubeIndex |= 1;
					if (isValidVoxel(x + 1, y + 1, z))
						cubeIndex |= 2;
					if (isValidVoxel(x, y, z + 1))
						cubeIndex |= 128;
					if (isValidVoxel(x + 1, y, z + 1))
						cubeIndex |= 64;
					if (isValidVoxel(x, y + 1, z + 1))
						cubeIndex |= 8;
					if (isValidVoxel(x + 1, y + 1, z + 1))
						cubeIndex |= 4;

					int edgeVal = MarchingCubesTable.EDGE_TABLE[cubeIndex];
					if (edgeVal == 0) {
						//Out.print("Oops @ %d, %d, %d (%f)", x, y, z, chunk.get(x, y, z));
					} else {
						Vector3 vertlist[] = new Vector3[12];

						if ((edgeVal & 1) > 0)
							vertlist[0] = new Vector3(x + 0.5f, y + 1, z);
						if ((edgeVal & 2) > 0)
							vertlist[1] = new Vector3(x + 1, y + 1, z + 0.5f);
						if ((edgeVal & 4) > 0)
							vertlist[2] = new Vector3(x + 0.5f, y + 1, z + 1);
						if ((edgeVal & 8) > 0)
							vertlist[3] = new Vector3(x, y + 1, z + 0.5f);
						if ((edgeVal & 16) > 0)
							vertlist[4] = new Vector3(x + 0.5f, y, z);
						if ((edgeVal & 32) > 0)
							vertlist[5] = new Vector3(x + 1, y, z + 0.5f);
						if ((edgeVal & 64) > 0)
							vertlist[6] = new Vector3(x + 0.5f, y, z + 1);
						if ((edgeVal & 128) > 0)
							vertlist[7] = new Vector3(x, y, z + 0.5f);
						if ((edgeVal & 256) > 0)
							vertlist[8] = new Vector3(x, y + 0.5f, z);
						if ((edgeVal & 512) > 0)
							vertlist[9] = new Vector3(x + 1, y + 0.5f, z);
						if ((edgeVal & 1024) > 0)
							vertlist[10] = new Vector3(x + 1, y + 0.5f, z + 1);
						if ((edgeVal & 2048) > 0)
							vertlist[11] = new Vector3(x, y + 0.5f, z + 1);

						int[] tris = MarchingCubesTable.TRI_TABLE[cubeIndex];
						for (int i = 0; tris[i] != -1; i += 3) {
							vertices.add(vertlist[tris[i]]);
							vertices.add(vertlist[tris[i + 1]]);
							vertices.add(vertlist[tris[i + 2]]);
						}
					}
				}
			}
		}

		int numVertices = vertices.size();
		/* Vertices + normals */
		float vertArr[] = new float[numVertices * 6];
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

					/* Sum all face normals for vertices they're touching */
					addToVertexNormals(vertArr, vertices, a, b, c, faceNormal, v);
				}
			}
			/* Normalize summed face normals */
			finalizeVertexNormals(vertArr);
		}
		return vertArr;
	}

	private void finalizeVertexNormals(float[] verts) {
		/* 0  1  2  3  4  5  6  7  8  9  10 11 */
		/* vx vy vz nx ny nz vx vy vz nx ny nz ... */
		Vector3 normal = new Vector3();
		for (int i = 3; i < verts.length; i += 6) {
			normal.set(verts[i], verts[i + 1], verts[i + 2]).nor();
			verts[i] = normal.x;
			verts[i + 1] = normal.y;
			verts[i + 2] = normal.z;
		}
	}

	private void addToVertexNormals(
			float[] verts, List<Vector3> vertList,
			Vector3 a, Vector3 b, Vector3 c,
			Vector3 faceNormal,
			int stopAt) {
		for (int i = 0; i <= stopAt; ++i) {
			Vector3 vert = vertList.get(i);
			if (vert.equals(a) || vert.equals(b) || vert.equals(c)) {
				int norIdx = i * 3 + 3;
				verts[norIdx] += faceNormal.x;
				verts[norIdx + 1] += faceNormal.y;
				verts[norIdx + 2] += faceNormal.z;
			}
		}
	}

	private boolean isValidVoxel(int x, int y, int z) {
		return 1f / (x + y + z) + chunk.get(x, y, z) >= isoLevel;
	}
}