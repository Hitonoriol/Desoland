package hitonoriol.voxelsandbox.util.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Async {
	private static final ExecutorService executor = Executors
			.newFixedThreadPool((int) (Runtime.getRuntime().availableProcessors()), new DaemonThreadFactory());
	
	public static ExecutorService executor() {
		return executor;
	}
}
