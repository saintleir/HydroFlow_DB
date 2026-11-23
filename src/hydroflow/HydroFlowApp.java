package hydroflow;

import hydroflow.ui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

// LOGIN:  ID = admin   PW = admin123

public class HydroFlowApp {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            error.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    error.getMessage() == null ? error.toString() : error.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE));
        });

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
