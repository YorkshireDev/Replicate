package Copy;

import GUI.ViewGUI;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ControllerCopy implements Runnable {

    static CountDownLatch copyLatch;

    private final ViewGUI viewGUI;

    private final boolean useThreading;
    private final boolean doChecksum;
    private final int retryCount;

    private final File destDir;
    private final List<File> fileList;

    public ControllerCopy(ViewGUI viewGUI,
                          boolean useThreading,
                          boolean doChecksum,
                          int retryCount,
                          File destDir,
                          DefaultListModel<String> transferListModel) {

        this.viewGUI = viewGUI;

        this.useThreading = useThreading;
        this.doChecksum = doChecksum;
        this.retryCount = retryCount;

        this.destDir = destDir;

        this.fileList = new ArrayList<>();
        for (int i = 0; i < transferListModel.size(); i++) fileList.add(new File(transferListModel.elementAt(i)));

    }

    @Override
    public void run() {

        ModelQueue.initFileQueueSystem(useThreading);

        List<File[]> srcDstList = new ModelFilter(fileList, destDir).getFilteredFileList();

        List<String> tmpTransferList = new ArrayList<>(srcDstList.stream().map(x -> x[0].getAbsolutePath()).toList());
        List<String> tmpCompletedList = new ArrayList<>();

        this.viewGUI.updateView(tmpTransferList, tmpCompletedList, "N/A");

        List<List<ModelCopy>> modelCopyPriorityList = new ArrayList<>();
        modelCopyPriorityList.add(new ArrayList<>());
        modelCopyPriorityList.add(new ArrayList<>());
        modelCopyPriorityList.add(new ArrayList<>());

        for (File[] currentSrcDstFile : srcDstList) {

            try {

                double fileSize = useThreading ? Files.size(currentSrcDstFile[0].toPath()) : 1099511627776L;
                fileSize *= 0.00000095367431640625d; // B to MiB

                ModelCopy modelCopy = new ModelCopy(currentSrcDstFile[0], currentSrcDstFile[1], doChecksum, retryCount);

                if (fileSize <= 16) modelCopyPriorityList.get(0).add(modelCopy); // Small File Size
                else if (fileSize > 16 && fileSize <= 1024) modelCopyPriorityList.get(1).add(modelCopy); // Medium File Size
                else modelCopyPriorityList.get(2).add(modelCopy); // Large File Size (or useThreading == False)

            } catch (IOException e) { throw new RuntimeException(e); }

        }

        int entireJobCompleteAmount = 0;
        int entireJobTotalAmount = srcDstList.size();

        for (int qT = 0; qT < modelCopyPriorityList.size(); qT++) {

            copyLatch = new CountDownLatch(modelCopyPriorityList.get(qT).size());

            Set<ModelCopy> completedModelCopySet = new HashSet<>();

            int currentQueueCompleteAmount = 0;
            int currentQueueTotalAmount = modelCopyPriorityList.get(qT).size();

            for (ModelCopy modelCopy : modelCopyPriorityList.get(qT)) ModelQueue.addToFileQueue(qT, modelCopy);
            ModelQueue.lockQueueSystem(qT);

            while (currentQueueCompleteAmount != currentQueueTotalAmount) {

                for (ModelCopy modelCopy : modelCopyPriorityList.get(qT)) {

                    if (completedModelCopySet.contains(modelCopy)) continue;

                    String finishedSrcFileString = modelCopy.getFinishedSrcFileString();

                    if (finishedSrcFileString == null) continue;

                    tmpTransferList.remove(finishedSrcFileString);
                    tmpCompletedList.add(modelCopy.getFinishedDestFileString());
                    completedModelCopySet.add(modelCopy);
                    String progressString = ++entireJobCompleteAmount + " / " + entireJobTotalAmount;
                    this.viewGUI.updateView(tmpTransferList, tmpCompletedList, progressString);
                    currentQueueCompleteAmount++;

                }

                try { Thread.sleep(128); }
                catch (InterruptedException e) { throw new RuntimeException(e); }

            }

            try { copyLatch.await(); }
            catch (InterruptedException e) { throw new RuntimeException(e); }

        }

        this.viewGUI.updateConfirmButton();
        ViewGUI.guiLatch.countDown();

    }

}
