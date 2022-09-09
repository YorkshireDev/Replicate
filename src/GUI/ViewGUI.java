package GUI;

import Copy.ControllerCopy;
import Copy.ModelCopy;
import Queue.ModelQueue;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewGUI extends JFrame {

    private JPanel panelMain;
    private JLabel labelCopyList;
    private JLabel labelHashingList;
    private JLabel labelCompletedList;
    private JCheckBox checkBoxVerifyChecksum;
    private JCheckBox checkBoxUseThreading;
    private JButton buttonSelectSource;
    private JButton buttonConfirm;
    private JTextArea textAreaCopyList;
    private JTextArea textAreaChecksumList;
    private JTextArea textAreaCompletedList;
    private JButton buttonSelectDestination;
    private JLabel labelDestination;
    private JScrollPane scrollPaneCopyList;
    private JScrollPane scrollPaneChecksumList;
    private JScrollPane scrollPaneCompletedList;
    private JProgressBar progressBarCompleted;

    private boolean verifyChecksum;
    private int threadCount;

    private File previousDirectory = null;

    private ExecutorService controllerService;
    public static boolean controllerRunning;
    private static boolean shutdownGUIService;

    private int copyListAmount;

    public ViewGUI() {

        SwingUtilities.invokeLater(this::initUserInterface);

        checkBoxVerifyChecksum.addActionListener(e -> {

            this.verifyChecksum = checkBoxVerifyChecksum.isSelected();
            this.textAreaChecksumList.setVisible(verifyChecksum);

        });

        checkBoxUseThreading.addActionListener(e ->
                this.threadCount = checkBoxUseThreading.isSelected() ? Runtime.getRuntime().availableProcessors() : 1);

        buttonSelectSource.addActionListener(e -> {

            if (controllerRunning) return;

            if (! ModelQueue.retrieve(1).isEmpty() || ! ModelQueue.retrieve(2).isEmpty()) {

                ModelQueue.reset();
                this.copyListAmount = 0;

            }

            JFileChooser fileDirChooser = new JFileChooser();

            fileDirChooser.setMultiSelectionEnabled(true);
            fileDirChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileDirChooser.setDialogTitle("Select File(s) or Folder(s)");
            if (previousDirectory != null) fileDirChooser.setCurrentDirectory(previousDirectory);

            int fileDirChoice = fileDirChooser.showDialog(this, "Select");

            if (fileDirChoice == JFileChooser.APPROVE_OPTION) {

                this.previousDirectory = fileDirChooser.getCurrentDirectory();

                File[] selectedFileDirArray = fileDirChooser.getSelectedFiles();

                for (File selectedFileDir : selectedFileDirArray)
                    ModelQueue.update(selectedFileDir.getAbsolutePath(), 0);

            }

        });

        buttonSelectDestination.addActionListener(e -> {

            if (controllerRunning) return;

            JFileChooser destChooser = new JFileChooser();

            destChooser.setMultiSelectionEnabled(false);
            destChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            destChooser.setDialogTitle("Select Destination Directory");

            int destChoice = destChooser.showDialog(this, "Select");

            if (destChoice == JFileChooser.APPROVE_OPTION)
                this.labelDestination.setText("Destination: " + destChooser.getSelectedFile().getAbsolutePath());

        });

        buttonConfirm.addActionListener(e -> {

            if (ModelQueue.retrieve(0).isEmpty() || controllerRunning) return;

            String destinationDir = labelDestination.getText().split("Destination: ")[1];
            if (destinationDir.equals("N/A")) return;

            controllerRunning = true;

            SwingUtilities.invokeLater(() -> {

                this.progressBarCompleted.setMaximum(ModelQueue.retrieve(0).size());
                this.copyListAmount = progressBarCompleted.getMaximum();

            });

            controllerService.submit(new ControllerCopy(verifyChecksum, threadCount, destinationDir));

        });

    }

    private final Runnable controllerGUIUpdate = () -> { // "Micro" controller for updating the three GUI lists.

        while (! shutdownGUIService) {

            try {

                for (int i = 0; i < 3; i++) { // Three GUI lists to loop over.

                    StringBuilder sBuilder = new StringBuilder();

                    for (String dirFileName : ModelQueue.retrieve(i))
                        sBuilder.append(dirFileName).append(System.lineSeparator());

                    if (sBuilder.length() > 0) sBuilder.deleteCharAt(sBuilder.length() - 1);

                    switch (i) {

                        case 0 -> SwingUtilities.invokeLater(() -> this.textAreaCopyList.setText(sBuilder.toString()));

                        case 1 -> SwingUtilities.invokeLater(() -> this.textAreaChecksumList.setText(sBuilder.toString()));

                        case 2 -> SwingUtilities.invokeLater(() -> this.textAreaCompletedList.setText(sBuilder.toString()));

                    }

                }

                if (copyListAmount > 0)
                    SwingUtilities.invokeLater(() -> this.progressBarCompleted.setValue(ModelQueue.retrieve(2).size()));
                else progressBarCompleted.setValue(0);

                Thread.sleep(512L);

            } catch (InterruptedException ignored) { return; }

        }

    };

    private void initUserInterface() {

        this.labelDestination.setText("Destination: N/A");

        this.labelCopyList.setText("Copy List");
        this.labelHashingList.setText("Checksum List");
        this.labelCompletedList.setText("Completed List");

        this.checkBoxVerifyChecksum.setText("Verify Checksum");
        this.checkBoxUseThreading.setText("Use Threading");

        this.buttonSelectSource.setText("Source");
        this.buttonSelectDestination.setText("Destination");
        this.buttonConfirm.setText("Confirm");

        this.verifyChecksum = true;
        this.threadCount = Runtime.getRuntime().availableProcessors();

        shutdownGUIService = false;
        controllerRunning = false;
        this.controllerService = Executors.newFixedThreadPool(2); // 1 is reserved for GUI list updating!
        this.controllerService.submit(controllerGUIUpdate);

        this.setContentPane(panelMain);
        this.setTitle("Replicate");
        this.setPreferredSize(new Dimension(640, 480));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);

    }

}
