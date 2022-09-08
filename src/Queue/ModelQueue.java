package Queue;

import java.util.concurrent.ConcurrentSkipListSet;

public class ModelQueue {

    private static ConcurrentSkipListSet<String> copyListSet;
    private static ConcurrentSkipListSet<String> checksumListSet;
    private static ConcurrentSkipListSet<String> completedListSet;

    public static void update(String currentDirFile, int sID) {

        switch (sID) {

            case 0 -> {

                checksumListSet.remove(currentDirFile);
                completedListSet.remove(currentDirFile);

                copyListSet.add(currentDirFile);

            }

            case 1 -> {

                copyListSet.remove(currentDirFile);
                completedListSet.remove(currentDirFile);

                checksumListSet.add(currentDirFile);

            }

            case 2 -> {

                copyListSet.remove(currentDirFile);
                checksumListSet.remove(currentDirFile);

                completedListSet.add(currentDirFile);

            }

        }

    }

    public static ConcurrentSkipListSet<String> retrieve(int sID) {

        switch (sID) {

            case 0 -> { return copyListSet; }

            case 1 -> { return checksumListSet; }

            case 2 -> { return completedListSet; }

        }

        return new ConcurrentSkipListSet<>();

    }

    public static void reset() {

        copyListSet = new ConcurrentSkipListSet<>();
        checksumListSet = new ConcurrentSkipListSet<>();
        completedListSet = new ConcurrentSkipListSet<>();

    }

}
