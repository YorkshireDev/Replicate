package GUI;

import Copy.ControllerCopy;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewGUI extends JFrame {

    private JPanel panelMain;
    private JPanel panelHomeMenu;
    private JPanel panelOptionMenu;

    private JButton buttonHomeScreen;
    private JButton buttonOptionScreen;
    private JButton buttonConfirm;
    private JCheckBox checkBoxUseThreading;
    private JCheckBox checkBoxDoChecksum;
    private JLabel labelRetryAmount;
    private JSpinner spinnerRetryAmount;
    private JButton buttonSelectSource;
    private JButton buttonSelectDestination;
    private JScrollPane scrollPaneTransferList;
    private JScrollPane scrollPaneCompletedList;
    private JLabel labelDestinationDirectory;
    private JLabel labelCompletedCount;

    private static final String[] BUTTON_CONFIRM_STATE_LIST = {"Start", "In-Progress", "Done"};

    private static final int RETRY_AMOUNT_MAX_VALUE = 32;

    private static final JFileChooser FILE_CHOOSER = new JFileChooser();
    private File fileChooserCurrentDirectory = null;

    private final DefaultListModel<String> transferListModel = new DefaultListModel<>();
    private final Set<String> transferListSet = new HashSet<>();
    private final JList<String> transferList = new JList<>(transferListModel);

    private final DefaultListModel<String> completedListModel = new DefaultListModel<>();
    private final JList<String> completedList = new JList<>(completedListModel);

    public static CountDownLatch guiLatch;

    public ViewGUI() {

        SwingUtilities.invokeLater(this::initUserInterface);

        buttonHomeScreen.addActionListener(e -> {

            this.panelOptionMenu.setVisible(false);
            this.panelHomeMenu.setVisible(true);

        });

        buttonOptionScreen.addActionListener(e -> {

            this.panelHomeMenu.setVisible(false);
            this.panelOptionMenu.setVisible(true);

        });

        buttonSelectSource.addActionListener(e -> {

            if (guiLatch.getCount() > 0) return;

            if (fileChooserCurrentDirectory != null) FILE_CHOOSER.setCurrentDirectory(fileChooserCurrentDirectory);

            FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            FILE_CHOOSER.setMultiSelectionEnabled(true);
            FILE_CHOOSER.setDialogTitle("Select Source File/Files or Directory/Directories");

            if (FILE_CHOOSER.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {

                File[] sourceList = FILE_CHOOSER.getSelectedFiles();

                if (sourceList != null) {

                    this.transferListSet.clear();
                    this.transferListModel.clear();
                    this.completedListModel.clear();

                    String destinationDir = labelDestinationDirectory.getText();

                    for (File sourceItem : sourceList) {

                        String sourceItemString = sourceItem.getAbsolutePath();

                        if (transferListSet.contains(sourceItemString)) continue;
                        if (sourceItemString.equals(destinationDir)) continue;

                        transferListSet.add(sourceItemString);

                        SwingUtilities.invokeLater(() -> {

                            this.transferListModel.addElement(sourceItemString);
                            this.buttonConfirm.setText(BUTTON_CONFIRM_STATE_LIST[0]);

                        });

                    }

                }

                fileChooserCurrentDirectory = new File(FILE_CHOOSER.getCurrentDirectory().getAbsolutePath());

            }

        });

        buttonSelectDestination.addActionListener(e -> {

            if (guiLatch.getCount() > 0) return;

            if (fileChooserCurrentDirectory != null) FILE_CHOOSER.setCurrentDirectory(fileChooserCurrentDirectory);

            FILE_CHOOSER.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            FILE_CHOOSER.setMultiSelectionEnabled(false);
            FILE_CHOOSER.setDialogTitle("Select Destination Directory");

            if (FILE_CHOOSER.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {

                String destinationDir = FILE_CHOOSER.getSelectedFile().getAbsolutePath();

                if (transferListSet.contains(destinationDir)) return;

                this.labelDestinationDirectory.setText(destinationDir);

                fileChooserCurrentDirectory = new File(FILE_CHOOSER.getCurrentDirectory().getAbsolutePath());

            }

        });

        buttonConfirm.addActionListener(e -> {

            if (guiLatch.getCount() > 0) return;

            if (transferListModel.isEmpty()) return;
            if (labelDestinationDirectory.getText().equals("N/A")) return;

            guiLatch = new CountDownLatch(1);

            boolean useThreading = checkBoxUseThreading.isSelected();
            boolean doChecksum = checkBoxDoChecksum.isSelected();
            int retryCount = (int) spinnerRetryAmount.getValue();

            this.buttonConfirm.setText(BUTTON_CONFIRM_STATE_LIST[1]);

            ExecutorService controllerService = Executors.newSingleThreadExecutor();
            controllerService.submit(new ControllerCopy(this, useThreading, doChecksum, retryCount, new File(labelDestinationDirectory.getText()), transferListModel));

        });

        spinnerRetryAmount.addChangeListener(e -> {

            int retryAmount = (int) spinnerRetryAmount.getValue();

            if (retryAmount >= RETRY_AMOUNT_MAX_VALUE) spinnerRetryAmount.setValue(RETRY_AMOUNT_MAX_VALUE);
            else if (retryAmount < 0) spinnerRetryAmount.setValue(-1);

        });

    }

    public void updateView(List<String> currentTransferList, List<String> currentCompletedList, String progressString) {

        SwingUtilities.invokeLater(() -> {

            this.transferListModel.clear();
            this.transferListModel.addAll(currentTransferList);

            this.completedListModel.clear();
            this.completedListModel.addAll(currentCompletedList);

            this.labelCompletedCount.setText(progressString);

        });

    }

    public void updateConfirmButton() {

        SwingUtilities.invokeLater(() -> this.buttonConfirm.setText(BUTTON_CONFIRM_STATE_LIST[2]));

    }

    private void initUserInterface() {

        guiLatch = new CountDownLatch(0);

        this.buttonHomeScreen.setText("Home");
        this.buttonOptionScreen.setText("Option");
        this.buttonConfirm.setText(BUTTON_CONFIRM_STATE_LIST[0]);

        this.checkBoxUseThreading.setText("Use Threading");
        this.checkBoxUseThreading.setSelected(true);
        this.checkBoxDoChecksum.setText("Do Checksum");
        this.checkBoxDoChecksum.setSelected(true);
        this.labelRetryAmount.setText("Retry Amount:");
        this.spinnerRetryAmount.setValue(8);

        this.buttonSelectSource.setText("Select Source");
        this.buttonSelectDestination.setText("Select Destination");

        this.scrollPaneTransferList.setViewportView(transferList);
        this.transferList.setLayoutOrientation(JList.VERTICAL);

        this.scrollPaneCompletedList.setViewportView(completedList);
        this.completedList.setLayoutOrientation(JList.VERTICAL);

        this.labelDestinationDirectory.setText("N/A");
        labelCompletedCount.setText("N/A");

        this.setContentPane(panelMain);
        this.setTitle("Replicate");
        this.setPreferredSize(new Dimension(800, 600));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);

    }

}
