package Copy;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class ModelFilter {

    private final List<File> fileList;
    private final File dstDir;

    ModelFilter(List<File> fileList, File dstDir) {

        this.fileList = fileList;
        this.dstDir = dstDir;

    }

    List<File[]> getFilteredFileList() {

        Stack<File> itemDirStack = new Stack<>();
        itemDirStack.addAll(fileList);

        List<File> rootDirList = new ArrayList<>(fileList);

        List<File[]> srcDstList = new ArrayList<>();
        int srcDstListIndex = 0;

        while (! itemDirStack.isEmpty()) {

            File currentItemFile = itemDirStack.pop();

            if (currentItemFile.isFile()) {

                srcDstList.add(new File[2]);

                srcDstList.get(srcDstListIndex)[0] = currentItemFile;

                for (File rootDir : rootDirList) {
                    if (currentItemFile.getAbsolutePath().contains(rootDir.getAbsolutePath())) {
                        String rootDirReplace;
                        try { rootDirReplace = rootDir.getParentFile().getAbsolutePath(); }
                        catch (Exception ignored) { rootDirReplace = rootDir.getAbsolutePath(); }
                        String dstDirString = currentItemFile.getAbsolutePath().replace(rootDirReplace, dstDir.getAbsolutePath() + File.separator);
                        File currentDstItemFile = new File(dstDirString).getAbsoluteFile();
                        srcDstList.get(srcDstListIndex)[1] = currentDstItemFile;
                        break;
                    }
                }

                srcDstListIndex++;

            } else {

                File[] nextDir = currentItemFile.listFiles();

                if (nextDir != null) {
                    for (File nextDirFile : nextDir) itemDirStack.push(nextDirFile);
                }

            }

        }

        for (File[] currentSrcDstFile : srcDstList) {

            try { FileUtils.createParentDirectories(currentSrcDstFile[1]); }
            catch (IOException e) { throw new RuntimeException(e); }

        }

        return srcDstList;

    }

}
