package hitonoriol.voxelsandbox.voxel;

import static hitonoriol.voxelsandbox.VoxelSandbox.world;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import hitonoriol.voxelsandbox.io.Out;
import hitonoriol.voxelsandbox.random.Random;
import hitonoriol.voxelsandbox.util.async.Async;
import hitonoriol.voxelsandbox.world.World;

public class Terrain implements RenderableProvider {
	private final static int CHUNK_SIZE = 10, CHUNK_HEIGHT = 20;
	public final static float CHUNK_VISUAL_SIZE = CHUNK_SIZE * Chunk.HORIZONTAL_SCALE;
	public final static float CHUNK_VISUAL_HEIGHT = CHUNK_HEIGHT * Chunk.VERTICAL_SCALE;

	private World world;
	private long seed = Random.nextLong();
	private int size;
	private Chunk chunks[];

	public Terrain(World world, int size) {
		this.world = world;
		this.size = size;
		chunks = new Chunk[size * size];
		for (int x = 0; x < size; ++x) {
			for (int z = 0; z < size; ++z) {
				int idx = getIdx(x, z);
				chunks[idx] = new Chunk(CHUNK_SIZE, CHUNK_HEIGHT);
				chunks[idx].setWorldPosition(x, z);
			}
		}
	}

	private void iterateChunkRect(int x, int z, int w, int h, Consumer<Chunk> action) {
		Out.print("Chunk rectangle: %d, %x [%dx%d]", x, z, w, h);
		int startX = x;
		int maxX = x + w, maxZ = z + h;
		for (; x < maxX; ++x) {
			action.accept(getChunk(x, z));
			if (h > 1)
				action.accept(getChunk(x, z + h - 1));
		}
		if (h < 3)
			return;
		x = startX;
		++z;
		for (; z < maxZ; ++z) {
			action.accept(getChunk(x, z));
			if (w > 1)
				action.accept(getChunk(x + w - 1, z));
		}
	}

	public void generate() {
		Out.print("Generating world (%d chunks) with seed %d", chunks.length, seed);
		for (int diag = 0, i = size; diag <= size && i > 0; i -= 2, ++diag) {
			final int fDiag = diag, fi = i;
			CompletableFuture.runAsync(() -> {
				iterateChunkRect(fDiag, fDiag, fi, fi, chunk -> {
					chunk.generate(seed);
				});
			}, Async.executor())
					.exceptionally(e -> {
						e.printStackTrace();
						return null;
					});
		}
	}

	public void updateChunk(Chunk chunk) {
		chunk.beginUpdate();
		CompletableFuture.runAsync(() -> {
			chunk.updateMesh();
		}, Async.executor())
				.thenRun(() -> {
					var dynWorld = world.getDynamicsWorld();
					var body = chunk.getBody();
					dynWorld.removeRigidBody(body);
					dynWorld.addRigidBody(body);
				})
				.exceptionally(e -> {
					Out.print("Exception on chunk update [%s]", chunk);
					e.printStackTrace();
					return null;
				});
	}

	public void updateChunk(int x, int z) {
		updateChunk(getChunk(x, z));
	}

	public void forEachChunk(Consumer<Chunk> action) {
		for (Chunk chunk : chunks)
			action.accept(chunk);
	}

	private int getIdx(int x, int z) {
		return x + z * size;
	}

	public Chunk getChunk(int x, int z) {
		return chunks[x + z * size];
	}

	public int getWorldSize() {
		return size;
	}

	public float getVisualSize() {
		return size * CHUNK_VISUAL_SIZE;
	}

	public float getVisualCenter() {
		return 0.5f * getVisualSize();
	}

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		for (int i = 0; i < chunks.length; ++i) {
			var chunk = chunks[i];
			if (chunk.needsUpdate())
				updateChunk(chunk);

			if (!chunk.isReady())
				continue;

			var renderable = pool.obtain();
			renderable.meshPart.set(chunk.getMeshPart());
			renderable.material = chunk.getMaterial();
			renderable.environment = world().environment;
			renderable.worldTransform.set(chunk.getTransform());
			renderables.add(renderable);
		}
	}
}
