package hitonoriol.voxelsandbox.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

public class GameInput extends InputMultiplexer {
	private final static GameInput instance = new GameInput();

	private GameInput() {
		Gdx.input.setInputProcessor(this);
	}
	
	public static void register(InputProcessor processor) {
		instance.addProcessor(processor);
	}
	
	public static void unregister(InputProcessor processor) {
		instance.removeProcessor(processor);
	}
	
	public static GameInput get() {
		return instance;
	}
}
