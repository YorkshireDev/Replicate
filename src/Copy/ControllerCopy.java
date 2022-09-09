package Copy;

import GUI.ViewGUI;
import Queue.ModelQueue;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControllerCopy implements Runnable {

    private final boolean doChecksum;
    private final int threadCount;
    private final String destinationDir;

    static CountDownLatch modelLatch;

    public ControllerCopy(boolean doChecksum, int threadCount, String destinationDir) {

        this.doChecksum = doChecksum;
        this.threadCount = threadCount;
        this.destinationDir = destinationDir;

    }

    @Override
    public void run() {

        boolean successfulTransfer = false;
        ExecutorService modelService = Executors.newFixedThreadPool(threadCount);

        while (! successfulTransfer) {

            modelLatch = new CountDownLatch(ModelQueue.retrieve(0).size());

            for (String dirFile : ModelQueue.retrieve(0))
                modelService.submit(new ModelCopy(dirFile, doChecksum, new File(destinationDir)));

            try {

                modelLatch.await();
                successfulTransfer = ModelQueue.retrieve(0).isEmpty();

            }
            catch (InterruptedException e) { throw new RuntimeException(e); }

        }

        ViewGUI.controllerRunning = false;

    }

}
