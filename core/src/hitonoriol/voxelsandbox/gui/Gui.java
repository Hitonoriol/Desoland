package hitonoriol.voxelsandbox.gui;

import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Gui {
	private final static Viewport viewport = new ScreenViewport();

	public static Viewport viewport() {
		return viewport;
	}
}
