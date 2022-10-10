package hitonoriol.voxelsandbox.voxel;

import static hitonoriol.voxelsandbox.VoxelSandbox.world;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import hitonoriol.voxelsandbox.io.Out;
import hitonoriol.voxelsandbox.random.Random;

public class Terrain implements RenderableProvider {
	private final static int CHUNK_SIZE = 10, CHUNK_HEIGHT = 10;
	public final static float CHUNK_VISUAL_SIZE = CHUNK_SIZE * Chunk.HORIZONTAL_SCALE;

	private long seed = Random.nextLong();
	private int size;
	private Chunk chunks[];

	private static final ExecutorService executor = Executors
			.newFixedThreadPool((int) (Runtime.getRuntime().availableProcessors()));

	public Terrain(int size) {
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
		int startX = x;
		int maxX = x + w, maxZ = z + h;
		for (; x < maxX; ++x) {
			action.accept(getChunk(x, z));
			if (h > 1)
				action.accept(getChunk(x, z + h - 1));
		}
		x = startX;
		for (; z < maxZ; ++z) {
			action.accept(getChunk(x, z));
			if (w > 1)
				action.accept(getChunk(x + w - 1, z));
		}
	}

	public void generate() {
		Out.print("Generating world (%d chunks) with seed %d", chunks.length, seed);
		var chunks = new AtomicInteger(0);
		for (int diag = 0, i = size; diag <= size; i -= 2, ++diag) {
			final int fDiag = diag, fi = i;
			CompletableFuture.runAsync(() -> {
				Out.print("diag = %d", fDiag);
				iterateChunkRect(fDiag, fDiag, fi, fi, chunk -> {
					chunks.incrementAndGet();
					chunk.generate(seed);
				});
			}, executor);
		}
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
