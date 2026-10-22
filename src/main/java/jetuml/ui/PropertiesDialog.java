package jetuml.ui;

import jetuml.app.DiagramEditor;
import jetuml.model.EditorType;
import jetuml.model.Element;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;
import jetuml.render.RichText;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class PropertiesDialog extends JDialog {
    public PropertiesDialog(JFrame owner, Element element, DiagramEditor editor) {
        super(owner, "Properties", true);
        setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        Map<String, Supplier<Object>> readers = new LinkedHashMap<>();
        int row = 0;
        for (PropertyDescriptor d : element.descriptors()) {
            gc.gridx = 0;
            gc.gridy = row;
            gc.weightx = 0;
            form.add(new JLabel(d.label()), gc);
            gc.gridx = 1;
            gc.weightx = 1;
            JComponent field = editorFor(d, element, readers);
            form.add(field, gc);
            row++;
        }
        add(form, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> {
            apply(element, editor, readers);
            dispose();
        });
        cancel.addActionListener(e -> dispose());
        buttons.add(ok);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(getPreferredSize());
    }

    private JComponent editorFor(PropertyDescriptor d, Element element, Map<String, Supplier<Object>> readers) {
        Object current = currentValue(element, d);
        if (d.editor() == EditorType.BOOLEAN) {
            JCheckBox box = new JCheckBox();
            box.setSelected(Boolean.parseBoolean(String.valueOf(current)));
            readers.put(d.key(), box::isSelected);
            return box;
        }
        if (d.editor() == EditorType.ENUM) {
            JComboBox<String> combo = new JComboBox<>(d.enumValues().toArray(String[]::new));
            combo.setSelectedItem(String.valueOf(current));
            readers.put(d.key(), () -> combo.getSelectedItem());
            return combo;
        }
        if (d.editor() == EditorType.MULTILINE) {
            JTextArea area = new JTextArea(String.valueOf(current == null ? "" : current), 6, 28);
            bindGuillemets(area);
            readers.put(d.key(), area::getText);
            return new JScrollPane(area);
        }
        JTextField field = new JTextField(String.valueOf(current == null ? "" : current), 24);
        bindGuillemets(field);
        readers.put(d.key(), field::getText);
        return field;
    }

    private void bindGuillemets(JTextField field) {
        field.registerKeyboardAction(e -> {
            int pos = field.getCaretPosition();
            field.setText(RichText.insertGuillemets(field.getText(), pos));
            field.setCaretPosition(Math.min(pos + 1, field.getText().length()));
        }, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
    }

    private void bindGuillemets(JTextArea area) {
        area.registerKeyboardAction(e -> {
            int pos = area.getCaretPosition();
            area.setText(RichText.insertGuillemets(area.getText(), pos));
            area.setCaretPosition(Math.min(pos + 1, area.getText().length()));
        }, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
    }

    @SuppressWarnings("unchecked")
    private void apply(Element element, DiagramEditor editor, Map<String, Supplier<Object>> readers) {
        for (PropertyDescriptor d : element.descriptors()) {
            Object value = readers.get(d.key()).get();
            if (d.type() == Boolean.class) {
                editor.setProperty(element, new Property<>(d.key(), Boolean.class, Boolean.FALSE), Boolean.valueOf(String.valueOf(value)));
            } else if (d.type().isEnum()) {
                editor.setProperty(element, new Property<>(d.key(), (Class) String.class, ""), String.valueOf(value));
            } else {
                editor.setProperty(element, new Property<>(d.key(), (Class) d.type(), d.defaultValue()), value);
            }
        }
    }

    private Object currentValue(Element element, PropertyDescriptor d) {
        if (d.type() == Boolean.class) {
            return element.get(new Property<>(d.key(), Boolean.class, Boolean.FALSE));
        }
        if (d.type().isEnum()) {
            Object v = element.get(new Property<>(d.key(), String.class, String.valueOf(d.defaultValue())));
            return v;
        }
        return element.get(new Property<>(d.key(), (Class) d.type(), d.defaultValue()));
    }
}
