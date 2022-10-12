package hitonoriol.voxelsandbox.input;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

public class GameInput extends InputMultiplexer {
	private final static GameInput instance = new GameInput();

	private List<PollableInput> pollables = new ArrayList<PollableInput>();

	private GameInput() {
		Gdx.input.setInputProcessor(this);
	}

	public static void register(InputProcessor processor) {
		instance.addProcessor(processor);
	}

	public static void register(PollableInputAdapter pollable) {
		register((InputProcessor) pollable);
		instance.pollables.add(pollable);
	}

	public static void unregister(InputProcessor processor) {
		instance.removeProcessor(processor);
	}

	public static void unregister(PollableInputAdapter pollable) {
		unregister((InputProcessor) pollable);
		instance.pollables.remove(pollable);
	}

	public static void poll() {
		instance.pollables.forEach(PollableInput::pollKeys);
	}

	public static GameInput get() {
		return instance;
	}
}
