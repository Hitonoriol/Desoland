package hitonoriol.voxelsandbox.gui.stages;

import static hitonoriol.voxelsandbox.VoxelSandbox.player;
import static hitonoriol.voxelsandbox.VoxelSandbox.world;
import static java.lang.String.format;

import java.util.function.Supplier;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

import hitonoriol.voxelsandbox.util.Utils;

public class GameStage extends Stage {
	private VisTable root = new VisTable();

	private VisTable infoTable = new VisTable();
	private VisLabel coordsLabel = new VisLabel();
	private VisLabel directionLabel = new VisLabel();
	private VisLabel objectCountLabel = new VisLabel();

	public GameStage() {
		root.setFillParent(true);
		root.add(infoTable).grow().align(Align.bottomLeft).row();
		addActor(root);

		infoTable.align(Align.bottomLeft);
		infoTable.defaults().padRight(5);
		infoTable.add(coordsLabel);
		infoTable.add(directionLabel);
		infoTable.add(objectCountLabel);
		update(coordsLabel,
				() -> format("[World position: %s]", Utils.vectorString(player().getPosition())), 0.5f);
		update(directionLabel,
				() -> format("[Direction: %s]", Utils.vectorString(player().getDirection())), 0.5f);
		update(objectCountLabel,
				() -> format("[Physical objects: %d]", world().getDynamicsWorld().getNumCollisionObjects()), 0.5f);
	}

	private void update(Label label, Supplier<String> updater, float interval) {
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
