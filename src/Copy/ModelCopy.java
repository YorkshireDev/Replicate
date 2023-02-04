package Copy;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;

class ModelCopy implements Runnable {

    private final File srcFile;
    private final File dstFile;

    private final boolean doChecksum;
    private final int retryCount;

    private String finishedSrcFileString;

    ModelCopy(File srcFile, File dstFile, boolean doChecksum, int retryCount) {

        this.srcFile = srcFile;
        this.dstFile = dstFile;

        this.doChecksum = doChecksum;
        this.retryCount = retryCount;

        this.finishedSrcFileString = null;

    }

    String getFinishedSrcFileString() {
        return finishedSrcFileString;
    }

    String getFinishedDestFileString() {
        return dstFile.getAbsolutePath();
    }

    @Override
    public void run() {

        int retriesGlobal = 0;
        boolean operationSuccessful = false;

        while (! operationSuccessful) {

            String srcFileChecksum = null;
            String dstFileChecksum;

            if (doChecksum) {

                try {
                    InputStream srcFileStream = new BufferedInputStream(Files.newInputStream(srcFile.toPath()));
                    srcFileChecksum = DigestUtils.sha256Hex(srcFileStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            int retriesLocal = 0;
            boolean copySuccessful = false;

            while (! copySuccessful) {

                try {
                    FileUtils.copyFile(srcFile, dstFile);
                    copySuccessful = true;
                } catch (IOException e) {
                    if (retryCount > -1) {
                        if (retriesLocal++ >= retryCount) throw new RuntimeException(e);
                    }
                }

            }

            if (doChecksum) {

                try {
                    InputStream srcFileStream = Files.newInputStream(dstFile.toPath());
                    dstFileChecksum = DigestUtils.sha256Hex(srcFileStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println(srcFileChecksum + " <-> " + dstFileChecksum);

                if (srcFileChecksum.equals(dstFileChecksum)) operationSuccessful = true;
                else {
                    if (retryCount > -1) {
                        if (retriesGlobal++ >= retryCount) throw new RuntimeException("Checksum Failure!");
                    }
                }

            } else operationSuccessful = true;

        }

        System.out.println("Global Retries: " + retriesGlobal);

        this.finishedSrcFileString = srcFile.getAbsolutePath();
        ControllerCopy.copyLatch.countDown();

    }

}
