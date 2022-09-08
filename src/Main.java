import GUI.ViewGUI;
import Queue.ModelQueue;

import javax.swing.*;

public class Main {

    public static void main(String[] args) throws
            UnsupportedLookAndFeelException,
            ClassNotFoundException,
            InstantiationException,
            IllegalAccessException {

        ModelQueue.reset();

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(ViewGUI::new);

    }

}
