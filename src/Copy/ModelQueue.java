package Copy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelQueue {

    private static ExecutorService fileQueueSmall;
    private static ExecutorService fileQueueMedium;
    private static ExecutorService fileQueueLarge;

    public static void addToFileQueue(int queueType, Runnable modelCopy) {

        switch (queueType) {

            case 0 -> fileQueueSmall.submit(modelCopy);
            case 1 -> fileQueueMedium.submit(modelCopy);
            case 2 -> fileQueueLarge.submit(modelCopy);

        }

    }

    public static void lockQueueSystem(int queueType) {

        switch (queueType) {

            case 0 -> fileQueueSmall.shutdown();
            case 1 -> fileQueueMedium.shutdown();
            case 2 -> fileQueueLarge.shutdown();

        }

    }

    public static void initFileQueueSystem(boolean useThreading) {

        int CPUThreadCount = Runtime.getRuntime().availableProcessors();

        fileQueueSmall = Executors.newFixedThreadPool(CPUThreadCount);
        fileQueueMedium = Executors.newFixedThreadPool(CPUThreadCount < 6 ? CPUThreadCount == 1 ? 1 : 2 : CPUThreadCount / 2);
        fileQueueLarge = useThreading ? Executors.newFixedThreadPool(CPUThreadCount <= 8 ? CPUThreadCount == 1 ? 1 : 2 : CPUThreadCount / 4) : Executors.newSingleThreadExecutor();

    }

}
