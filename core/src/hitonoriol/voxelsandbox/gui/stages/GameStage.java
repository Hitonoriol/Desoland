package hitonoriol.voxelsandbox.gui.stages;

import static hitonoriol.voxelsandbox.VoxelSandbox.player;
import static hitonoriol.voxelsandbox.VoxelSandbox.world;
import static java.lang.String.format;

import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

import hitonoriol.voxelsandbox.assets.Assets;
import hitonoriol.voxelsandbox.gui.Gui;
import hitonoriol.voxelsandbox.util.Utils;
import hitonoriol.voxelsandbox.voxel.Terrain;

public class GameStage extends Stage {
	private VisTable root = new VisTable();

	private VisTable infoTable = new VisTable();
	private VisLabel coordsLabel = new VisLabel();
	private VisLabel directionLabel = new VisLabel();
	private VisLabel objectCountLabel = new VisLabel();
	private VisLabel fpsLabel = new VisLabel();
	
	private Color crosshairColor = Color.WHITE.cpy();
	private Image crosshair = new Image(Assets.load("textures/crosshair.png", Texture.class));

	public GameStage() {
		super(Gui.viewport());
		root.setFillParent(true);
		root.add(infoTable).grow().align(Align.bottomLeft).row();
		addActor(root);
		crosshair.setOrigin(Align.center);
		crosshairColor.a = 0.75f;
		crosshair.setColor(crosshairColor);
		addActor(crosshair);

		infoTable.align(Align.bottomLeft);
		infoTable.defaults().padRight(5);
		addInfo(coordsLabel,
				() -> {
					var position = player().getPosition();
					return format("[World position: %s (chunk %d, %d)]",
							Utils.vectorString(position),
							(int) (position.x / Terrain.CHUNK_VISUAL_SIZE), (int) (position.z / Terrain.CHUNK_VISUAL_SIZE));
				}, 0.5f);
		addInfo(directionLabel,
				() -> format("[Direction: %s]", Utils.vectorString(player().getDirection())), 0.5f);
		addInfo(objectCountLabel,
				() -> format("[Physical objects: %d]", world().getDynamicsWorld().getNumCollisionObjects()), 0.5f);
		addInfo(fpsLabel,
				() -> format("[FPS: %d]", Gdx.graphics.getFramesPerSecond()), 1f);
	}
	
	public void layout() {
		crosshair.setPosition(getWidth() / 2, getHeight() / 2);
	}
	
	private void addInfo(Label label, Supplier<String> updater, float interval) {
		infoTable.add(label);
		update(() -> label.setText(updater.get()), interval);
	}

	private void update(Runnable updater, float interval) {
		Timer.instance().scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				updater.run();
			}
		}, 0, interval);
	}
}
