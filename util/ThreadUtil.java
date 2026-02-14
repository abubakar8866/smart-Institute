package util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadUtil {

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    private ThreadUtil() { }

    public static void runAsync(Runnable task) {
        executor.submit(task);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
