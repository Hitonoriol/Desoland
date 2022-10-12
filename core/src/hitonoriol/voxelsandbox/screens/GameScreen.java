package hitonoriol.voxelsandbox.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;

import hitonoriol.voxelsandbox.gui.stages.GameStage;
import hitonoriol.voxelsandbox.input.GameInput;
import hitonoriol.voxelsandbox.world.World;

public class GameScreen extends ScreenAdapter {
	private GameStage overlay = new GameStage();
	private World world;

	public GameScreen() {
		world = new World();
	}

	@Override
	public void render(float delta) {
		GameInput.poll();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		world.update(delta);
		world.render();
		world.debugRender(world.camera);
		overlay.act();
		overlay.draw();
		super.render(delta);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		overlay.layout();
		world.updateViewport(width, height);
	}

	@Override
	public void dispose() {
		world.dispose();
	}

	public World getWorld() {
		return world;
	}
}
