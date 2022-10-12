package hitonoriol.voxelsandbox;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.kotcrab.vis.ui.VisUI;

import hitonoriol.voxelsandbox.assets.Assets;
import hitonoriol.voxelsandbox.entity.Player;
import hitonoriol.voxelsandbox.gui.Gui;
import hitonoriol.voxelsandbox.input.GameInput;
import hitonoriol.voxelsandbox.input.Keyboard;
import hitonoriol.voxelsandbox.screens.GameScreen;
import hitonoriol.voxelsandbox.world.World;

public class VoxelSandbox extends Game {
	private GameScreen gameScreen;
	private List<Runnable> initTasks = new ArrayList<>();

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
		finalizeInit();
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
	public void resize(int width, int height) {
		Gui.viewport().update(width, height, true);
		super.resize(width, height);
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
	
	public static void deferInit(Runnable task) {
		game.initTasks.add(task);
	}
	
	private void finalizeInit() {
		initTasks.forEach(Runnable::run);
		initTasks.clear();
	}
	
	@Override
	public void dispose() {
		gameScreen.dispose();
		Assets.manager().dispose();
		VisUI.dispose();
	}
}
