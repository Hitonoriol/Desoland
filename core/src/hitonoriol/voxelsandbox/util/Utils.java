package hitonoriol.voxelsandbox.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Utils {
	public static float clamp(float value, float absMax) {
		return MathUtils.clamp(value, -absMax, absMax);
	}

	public static String vectorString(Vector3 vector) {
		return String.format("%.1f, %.1f, %.1f", vector.x, vector.y, vector.z);
	}
}
