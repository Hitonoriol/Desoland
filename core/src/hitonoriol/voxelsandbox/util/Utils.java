package hitonoriol.voxelsandbox.util;

import com.badlogic.gdx.math.MathUtils;

public class Utils {
	public static float clamp(float value, float absMax) {
		return MathUtils.clamp(value, -absMax, absMax);
	}
}
