package hitonoriol.voxelsandbox.assets;

public class Prefs {
	public boolean firstPersonCamera = true;
	public float firstPersonVerticalFactor = 0.9f;
	public float firstPersonHorizontalDistance = 2f;
	public float thirdPersonHorizontalDistance = 13.5f;
	public float thirdPersonVerticalDistance = 5f;

	private static final Prefs instance = new Prefs();

	private Prefs() {}

	public static Prefs values() {
		return instance;
	}
}
