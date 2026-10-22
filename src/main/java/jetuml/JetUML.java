package jetuml;

import jetuml.app.Workspace;
import jetuml.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point.
 * Sets the system look-and-feel, creates the {@link Workspace}
 * and the {@link MainFrame}, and shows the window on the EDT.
 */
public final class JetUML {

    private JetUML() {}

    public static void main(String[] args) {
        // Use system look-and-feel for a native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the cross-platform LAF — no action needed
        }

        SwingUtilities.invokeLater(() -> {
            Workspace workspace = new Workspace();
            MainFrame frame = new MainFrame(workspace);
            frame.setVisible(true);
        });
    }
}
