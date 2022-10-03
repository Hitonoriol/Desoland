package hitonoriol.voxelsandbox;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.kotcrab.vis.ui.VisUI;

import hitonoriol.voxelsandbox.assets.Assets;
import hitonoriol.voxelsandbox.entity.Player;
import hitonoriol.voxelsandbox.input.GameInput;
import hitonoriol.voxelsandbox.input.Keyboard;
import hitonoriol.voxelsandbox.screens.GameScreen;
import hitonoriol.voxelsandbox.world.World;

public class VoxelSandbox extends Game {
	private GameScreen gameScreen;

	private static VoxelSandbox game;
	
	public VoxelSandbox() {
		game = this;
	}
	
	@Override
	public void create() {
		VisUI.load();
		Bullet.init();
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

	public static World world() {
		return game.gameScreen.getWorld();
	}
	
	public static Player player() {
		return game.gameScreen.getWorld().getPlayer();
	}
	
	public static VoxelSandbox game() {
		return game;
	}
	
	@Override
	public void dispose() {
		gameScreen.dispose();
		Assets.manager().dispose();
		VisUI.dispose();
	}
}
