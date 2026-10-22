package jetuml.ui;

import jetuml.app.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Dialog for editing application-level {@link Preferences}.
 * Covers grid, dark mode, Auto Edit Node, tooltips, font size,
 * notification duration and diagram size (RF36–RF42, §10 of DESING.md).
 */
public final class PreferencesDialog extends JDialog {

    public PreferencesDialog(JFrame owner) {
        super(owner, "Preferences", true);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        // Show Grid
        JCheckBox showGrid = new JCheckBox("Show Grid", Preferences.INSTANCE.showGrid());
        addRow(form, gc, row++, "Grid", showGrid);

        // Dark Mode
        JCheckBox darkMode = new JCheckBox("Dark Mode", Preferences.INSTANCE.darkMode());
        addRow(form, gc, row++, "Appearance", darkMode);

        // Auto Edit Node
        JCheckBox autoEdit = new JCheckBox("Auto Edit Node on Creation", Preferences.INSTANCE.autoEditNode());
        addRow(form, gc, row++, "Editing", autoEdit);

        // Show Tool Hint
        JCheckBox showHint = new JCheckBox("Show Tool Hint in Toolbar", Preferences.INSTANCE.showToolHint());
        addRow(form, gc, row++, "Toolbar", showHint);

        // Verbose Tooltips
        JCheckBox verbose = new JCheckBox("Verbose Tooltips", Preferences.INSTANCE.verboseTooltips());
        addRow(form, gc, row++, "Tooltips", verbose);

        // Font size
        JSpinner fontSize = new JSpinner(new SpinnerNumberModel(
                Preferences.INSTANCE.fontSize(), 8, 24, 1));
        addRow(form, gc, row++, "Font Size (pt)", fontSize);

        // Notification duration
        JSpinner notifDuration = new JSpinner(new SpinnerNumberModel(
                Preferences.INSTANCE.notificationDurationMs(), 0, 10000, 500));
        addRow(form, gc, row++, "Notification Duration (ms, 0=off)", notifDuration);

        // Diagram width
        JSpinner diagWidth = new JSpinner(new SpinnerNumberModel(
                Preferences.INSTANCE.diagramWidth(), 250, 4000, 50));
        addRow(form, gc, row++, "Default Diagram Width", diagWidth);

        // Diagram height
        JSpinner diagHeight = new JSpinner(new SpinnerNumberModel(
                Preferences.INSTANCE.diagramHeight(), 250, 4000, 50));
        addRow(form, gc, row++, "Default Diagram Height", diagHeight);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel();
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");

        ok.addActionListener(e -> {
            Preferences.INSTANCE.setShowGrid(showGrid.isSelected());
            Preferences.INSTANCE.setDarkMode(darkMode.isSelected());
            Preferences.INSTANCE.setAutoEditNode(autoEdit.isSelected());
            Preferences.INSTANCE.setShowToolHint(showHint.isSelected());
            Preferences.INSTANCE.setVerboseTooltips(verbose.isSelected());
            Preferences.INSTANCE.setFontSize((Integer) fontSize.getValue());
            Preferences.INSTANCE.setNotificationDurationMs((Integer) notifDuration.getValue());
            Preferences.INSTANCE.setDiagramSize((Integer) diagWidth.getValue(), (Integer) diagHeight.getValue());
            dispose();
        });
        cancel.addActionListener(e -> dispose());

        buttons.add(ok);
        buttons.add(cancel);
        getRootPane().setDefaultButton(ok);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String label, javax.swing.JComponent widget) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel(label + ":"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(widget, gc);
    }
}
