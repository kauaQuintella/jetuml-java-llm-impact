package jetuml.ui.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ActionRegistry {
    private final Map<String, AppAction> actions = new LinkedHashMap<>();

    public void register(AppAction action) {
        actions.put(action.id(), action);
    }

    public AppAction get(String id) {
        return actions.get(id);
    }

    public Map<String, AppAction> all() {
        return Map.copyOf(actions);
    }

    public Action swing(String id) {
        AppAction a = get(id);
        return new AbstractAction(a.label()) {
            {
                putValue(ACCELERATOR_KEY, a.accel());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (a.isEnabled()) {
                    a.run().run();
                }
            }

            @Override
            public boolean isEnabled() {
                return a.isEnabled();
            }
        };
    }

    public void bind(JComponent component, String id) {
        AppAction a = get(id);
        if (a == null || a.accel() == null) {
            return;
        }
        String key = "jetuml." + id;
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(a.accel(), key);
        component.getActionMap().put(key, swing(id));
    }

    public void bindAll(JComponent component) {
        for (String id : actions.keySet()) {
            bind(component, id);
        }
    }

    public static KeyStroke key(String spec) {
        return KeyStroke.getKeyStroke(spec);
    }
}
