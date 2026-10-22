package jetuml.ui;

import jetuml.app.Preferences;
import jetuml.model.types.ToolKind;
import jetuml.model.types.ToolSpec;
import jetuml.ui.tools.Tool;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ToolbarPanel extends JToolBar {
    private final ButtonGroup group = new ButtonGroup();
    private final Map<String, JToggleButton> buttons = new LinkedHashMap<>();

    public ToolbarPanel(List<ToolSpec> tools, Consumer<ToolSpec> onSelect) {
        setFloatable(false);
        setOrientation(VERTICAL);
        int index = 0;
        for (ToolSpec spec : tools) {
            String shortcut = ToolSpec.shortcutFor(index);
            JToggleButton b = new JToggleButton(spec.label());
            b.setToolTipText(tooltip(spec, shortcut));
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(180, 36));
            int captured = index;
            b.addActionListener(e -> onSelect.accept(spec));
            if (spec.kind() == ToolKind.SELECT) {
                b.setSelected(true);
            }
            group.add(b);
            buttons.put(spec.id(), b);
            add(b);
            index++;
            Preferences.INSTANCE.subscribe(p -> b.setToolTipText(tooltip(spec, shortcut)));
        }
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    private String tooltip(ToolSpec spec, String shortcut) {
        String base = spec.label() + " [" + shortcut + "]";
        if (Preferences.INSTANCE.verboseTooltips()) {
            return base + " — " + spec.verboseTooltip();
        }
        return Preferences.INSTANCE.showToolHint() ? base : spec.label();
    }

    public void select(String id) {
        JToggleButton b = buttons.get(id);
        if (b != null) {
            b.setSelected(true);
        }
    }

    public JPanel asPanel() {
        JPanel p = new JPanel();
        p.add(this);
        return p;
    }
}
