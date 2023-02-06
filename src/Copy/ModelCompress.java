package Copy;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

class ModelCompress {

    private final File srcFile;

    ModelCompress(File srcFile) {

        this.srcFile = srcFile;

    }

    void doCompression() {

        String zipFile = srcFile.getAbsolutePath();
        if (srcFile.isFile()) zipFile = zipFile.replace(zipFile.substring(zipFile.lastIndexOf('.')), ".zip");
        else zipFile += ".zip";

        try {

            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionLevel(CompressionLevel.MAXIMUM);

            if (srcFile.isFile()) {

                new ZipFile(zipFile).addFile(srcFile, zipParameters);
                FileUtils.delete(srcFile);

            }
            else {

                new ZipFile(zipFile).addFolder(srcFile, zipParameters);
                FileUtils.deleteDirectory(srcFile);

            }

        } catch (IOException e) { throw new RuntimeException(e); }

    }

}
