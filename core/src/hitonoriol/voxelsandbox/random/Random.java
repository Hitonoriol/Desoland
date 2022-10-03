package hitonoriol.voxelsandbox.random;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.badlogic.gdx.graphics.Color;

public class Random {
	private final static RandomDataGenerator generator = new RandomDataGenerator();

	public static float nextFloat(float min, float max) {
		return (float) generator.nextUniform(min, max, true);
	}
	
	public static Color randomColor() {
		return new Color(Random.nextFloat(0, 1), Random.nextFloat(0, 1), Random.nextFloat(0, 1), 1);
	}
	
	public static RandomDataGenerator get() {
		return generator;
	}

	public static RandomDataGenerator generator(long seed) {
		var rng = new RandomDataGenerator();
		rng.reSeed(seed);
		return rng;
	}
}
