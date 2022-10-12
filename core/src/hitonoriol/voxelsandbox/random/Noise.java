package hitonoriol.voxelsandbox.random;

public class Noise {
	private long seed = 0;

	private float scale = 375;
	private float persistence = 0.5f;
	private float lacunarity = 2;
	private float exponentiation = 3.7f;
	private float height = 64;
	private int octaves = 1;

	public Noise(long seed) {
		this.seed = seed;
	}
	
	public float generate(float x, float y, float z) {
		float xs = x / scale;
		float ys = y / scale;
		float zs = z / scale;
		float G = (float) Math.pow(2.0, -persistence);
		float amplitude = 1.0f;
		float frequency = 1.0f;
		float normalization = 0;
		float total = 0;
		for (int o = 0; o < octaves; ++o) {
			float noiseValue = OpenSimplex2.noise3_ImproveXZ(seed, xs * frequency, ys * frequency, zs * frequency)
					* 0.5f + 0.5f;
			total += noiseValue * amplitude;
			normalization += amplitude;
			amplitude *= G;
			frequency *= lacunarity;
		}
		total /= normalization;
		return (float) (Math.pow(total, exponentiation) * height);
	}

	public float generateSimple(float x, float y, float z) {
		return OpenSimplex2.noise3_ImproveXZ(seed, x, y, z);
	}
	
	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public float getPersistence() {
		return persistence;
	}

	public void setPersistence(float persistence) {
		this.persistence = persistence;
	}

	public float getLacunarity() {
		return lacunarity;
	}

	public void setLacunarity(float lacunarity) {
		this.lacunarity = lacunarity;
	}

	public float getExponentiation() {
		return exponentiation;
	}

	public void setExponentiation(float exponentiation) {
		this.exponentiation = exponentiation;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public int getOctaves() {
		return octaves;
	}

	public void setOctaves(int octaves) {
		this.octaves = octaves;
	}
}
