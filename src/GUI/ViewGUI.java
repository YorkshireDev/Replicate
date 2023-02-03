package GUI;

import javax.swing.*;
import java.awt.*;

public class ViewGUI extends JFrame {

    private JPanel panelMain;

    public ViewGUI() {

        SwingUtilities.invokeLater(this::initUserInterface);

    }

    private void initUserInterface() {

        this.setContentPane(panelMain);
        this.setTitle("Replicate");
        this.setPreferredSize(new Dimension(640, 480));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);

    }

}
