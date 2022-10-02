package hitonoriol.voxelsandbox;

import com.badlogic.gdx.Game;

import hitonoriol.voxelsandbox.input.GameInput;
import hitonoriol.voxelsandbox.input.Keyboard;
import hitonoriol.voxelsandbox.screens.GameScreen;

public class VoxelSandbox extends Game {
	private GameScreen gameScreen;

	@Override
	public void create() {
		createScreens();
		GameInput.register(Keyboard.get());
	}

	private void createScreens() {
		gameScreen = new GameScreen();
		setScreen(gameScreen);
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		gameScreen.dispose();
	}
}
