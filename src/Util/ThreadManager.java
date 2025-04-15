package src.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

// utility class for managing background threads in the application
// uses executorservice to handle thread creation and management
public class ThreadManager {
    // thread pool for executing background tasks
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    
    // flag to track if the manager has been shut down
    private static boolean isShutdown = false;
    
    // execute a task in the background and handle the result with a callback
    // @param task - the task to execute in the background
    // @param onSuccess - callback to handle successful completion
    // @param onError - callback to handle errors
    public static <T> void executeAsync(Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onError) {
        if (isShutdown) {
            onError.accept(new IllegalStateException("ThreadManager has been shut down"));
            return;
        }
        
        executor.submit(() -> {
            try {
                T result = task.call();
                if (onSuccess != null) {
                    onSuccess.accept(result);
                }
            } catch (Exception e) {
                Logger.log(Logger.ERROR, "Error in background task: " + e.getMessage());
                if (onError != null) {
                    onError.accept(e);
                }
            }
        });
    }
    
    // execute a task in the background and return a future for the result
    // @param task - the task to execute in the background
    // @return a future representing the pending result
    public static <T> Future<T> submitTask(Callable<T> task) {
        if (isShutdown) {
            throw new IllegalStateException("ThreadManager has been shut down");
        }
        return executor.submit(task);
    }
    
    // shutdown the thread manager and release resources
    // this should be called when the application is closing
    public static void shutdown() {
        if (!isShutdown) {
            executor.shutdown();
            isShutdown = true;
            Logger.log(Logger.INFO, "ThreadManager has been shut down");
        }
    }
}
