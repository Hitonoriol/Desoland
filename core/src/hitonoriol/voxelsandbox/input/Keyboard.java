package hitonoriol.voxelsandbox.input;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;

public class Keyboard extends InputAdapter {
	private Set<Integer> pressedKeys = new HashSet<>();

	private final static Keyboard instance = new Keyboard();

	private Keyboard() {}

	@Override
	public boolean keyDown(int keycode) {
		pressedKeys.add(keycode);
		return super.keyDown(keycode);
	}

	@Override
	public boolean keyUp(int key) {
		pressedKeys.remove(key);
		switch (key) {
		case Keys.ESCAPE:
			Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
		default:
			break;
		}
		return super.keyUp(key);
	}

	public static Set<Integer> getPressedKeys() {
		return instance.pressedKeys;
	}

	public static boolean isKeyPressed(int key) {
		return instance.pressedKeys.contains(key);
	}

	public static Keyboard get() {
		return instance;
	}
}
