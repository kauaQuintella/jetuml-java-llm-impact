package jetuml.ui;

import jetuml.app.Workspace;
import jetuml.model.types.DiagramType;
import jetuml.model.types.DiagramTypeRegistry;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Consumer;

public final class WelcomePanel extends JPanel {
    public WelcomePanel(Workspace workspace, Consumer<DiagramType> onNew) {
        setLayout(new BorderLayout(16, 16));
        JLabel title = new JLabel("JetUML", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 32f));
        add(title, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(0, 1, 8, 8));
        for (DiagramType type : DiagramTypeRegistry.all()) {
            JButton b = new JButton("New " + type.displayName());
            b.addActionListener(e -> onNew.accept(type));
            buttons.add(b);
        }
        add(buttons, BorderLayout.CENTER);
        add(new JLabel("Create a diagram or open a .json file from the File menu.", JLabel.CENTER), BorderLayout.SOUTH);
    }
}
