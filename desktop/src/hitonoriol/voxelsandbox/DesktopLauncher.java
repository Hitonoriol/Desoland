package hitonoriol.voxelsandbox;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.useVsync(true);
		config.setTitle("Voxel Sandbox");
		config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 8);
		config.setWindowSizeLimits(1280, 720, -1, -1);
		new Lwjgl3Application(new VoxelSandbox(), config);
	}
}
