package hitonoriol.voxelsandbox.util.async;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {
	@Override
	public Thread newThread(Runnable task) {
		Thread thread = Executors.defaultThreadFactory().newThread(task);
		thread.setDaemon(true);
		return thread;
	}
}
