package Copy;

import Queue.ModelQueue;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Stack;

public class ModelCopy implements Runnable {

    private final String dirFileString;
    private final boolean doChecksum;
    private final File destinationDir;

    ModelCopy(String dirFileString, boolean doChecksum, File destinationDir) {

        this.dirFileString = dirFileString;
        this.doChecksum = doChecksum;
        this.destinationDir = destinationDir;

    }

    private String getDirHashConcat(File rootDir) throws IOException {

        StringBuilder hashConcat = new StringBuilder();

        Stack<File> dirQueue = new Stack<>();
        dirQueue.push(rootDir);

        while (! dirQueue.isEmpty()) {

            File[] dirList = dirQueue.pop().listFiles();

            if (dirList != null) {

                for (File currentItem : dirList) {

                    if (currentItem.isDirectory()) dirQueue.push(currentItem);
                    else if (currentItem.isFile()) hashConcat.append(DigestUtils.sha256Hex(new BufferedInputStream(new FileInputStream(currentItem))));

                }

            }

        }

        return hashConcat.toString();

    }

    @Override
    public void run() {

        boolean successfulTransfer = false;

        while (! successfulTransfer) {

            File dirFile = new File(dirFileString);

            try {

                if (dirFile.isFile()) FileUtils.copyFileToDirectory(dirFile, destinationDir);
                else if (dirFile.isDirectory()) FileUtils.copyDirectoryToDirectory(dirFile, destinationDir);

                if (doChecksum) {

                    ModelQueue.update(dirFileString, 1);

                    String srcHash = "";
                    String destHash = "";

                    if (dirFile.isFile()) {

                        String destFile = destinationDir.getAbsolutePath() + File.separator + dirFile.getName();

                        srcHash = DigestUtils.sha256Hex(new BufferedInputStream(new FileInputStream(dirFile)));
                        destHash = DigestUtils.sha256Hex(new BufferedInputStream(new FileInputStream(destFile)));

                    } else if (dirFile.isDirectory()) {

                        String destDir = destinationDir.getAbsolutePath() + File.separator + dirFile.getName();

                        srcHash = DigestUtils.sha256Hex(getDirHashConcat(dirFile));
                        destHash = DigestUtils.sha256Hex(getDirHashConcat(new File(destDir)));

                    }

                    successfulTransfer = srcHash.equals(destHash);
                    if (! successfulTransfer) ModelQueue.update(dirFileString, 0);

                } else successfulTransfer = true;

                ModelQueue.update(dirFileString, 2);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        ControllerCopy.modelLatch.countDown();

    }

}
