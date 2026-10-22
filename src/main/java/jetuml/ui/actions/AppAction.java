package jetuml.ui.actions;

import javax.swing.KeyStroke;
import java.util.function.BooleanSupplier;

public record AppAction(String id, String label, KeyStroke accel, BooleanSupplier enabled, Runnable run) {
    public boolean isEnabled() {
        return enabled == null || enabled.getAsBoolean();
    }
}
