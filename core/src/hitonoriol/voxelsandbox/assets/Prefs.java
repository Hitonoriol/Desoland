package hitonoriol.voxelsandbox.assets;

public class Prefs {
	public final static boolean debug = false;
	
	public boolean firstPersonCamera = true;
	public float firstPersonVerticalFactor = 0.45f;
	public float firstPersonHorizontalDistance = 2f;
	public float thirdPersonHorizontalDistance = 13.5f;
	public float thirdPersonVerticalDistance = 1.5f;
	
	public float cameraFov = 75f;
	public float cameraSprintingFov = 95f;
	public float cameraViewDistance = 250f;

	private static final Prefs instance = new Prefs();

	private Prefs() {}

	public static Prefs values() {
		return instance;
	}
}
