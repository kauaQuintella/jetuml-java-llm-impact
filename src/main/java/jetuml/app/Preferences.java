package jetuml.app;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;

public final class Preferences {
    public static final Preferences INSTANCE = new Preferences();

    private final java.util.prefs.Preferences store = java.util.prefs.Preferences.userNodeForPackage(Preferences.class);
    private final List<Consumer<Preferences>> listeners = new CopyOnWriteArrayList<>();

    private boolean showGrid;
    private boolean autoEditNode;
    private boolean showToolHint;
    private boolean verboseTooltips;
    private boolean darkMode;
    private int fontSize;
    private int notificationDurationMs;
    private int diagramWidth;
    private int diagramHeight;
    private int zoomPercent;

    private Preferences() {
        showGrid = store.getBoolean("showGrid", true);
        autoEditNode = store.getBoolean("autoEditNode", true);
        showToolHint = store.getBoolean("showToolHint", true);
        verboseTooltips = store.getBoolean("verboseTooltips", false);
        darkMode = store.getBoolean("darkMode", false);
        fontSize = store.getInt("fontSize", 12);
        notificationDurationMs = store.getInt("notificationDurationMs", 3000);
        diagramWidth = store.getInt("diagramWidth", 1000);
        diagramHeight = store.getInt("diagramHeight", 700);
        zoomPercent = store.getInt("zoomPercent", 100);
    }

    public void subscribe(Consumer<Preferences> listener) {
        listeners.add(listener);
    }

    private void changed() {
        persist();
        for (Consumer<Preferences> l : listeners) {
            l.accept(this);
        }
    }

    private void persist() {
        store.putBoolean("showGrid", showGrid);
        store.putBoolean("autoEditNode", autoEditNode);
        store.putBoolean("showToolHint", showToolHint);
        store.putBoolean("verboseTooltips", verboseTooltips);
        store.putBoolean("darkMode", darkMode);
        store.putInt("fontSize", fontSize);
        store.putInt("notificationDurationMs", notificationDurationMs);
        store.putInt("diagramWidth", diagramWidth);
        store.putInt("diagramHeight", diagramHeight);
        store.putInt("zoomPercent", zoomPercent);
        try {
            store.flush();
        } catch (BackingStoreException ignored) {
        }
    }

    public boolean showGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        changed();
    }

    public boolean autoEditNode() {
        return autoEditNode;
    }

    public void setAutoEditNode(boolean autoEditNode) {
        this.autoEditNode = autoEditNode;
        changed();
    }

    public boolean showToolHint() {
        return showToolHint;
    }

    public void setShowToolHint(boolean showToolHint) {
        this.showToolHint = showToolHint;
        changed();
    }

    public boolean verboseTooltips() {
        return verboseTooltips;
    }

    public void setVerboseTooltips(boolean verboseTooltips) {
        this.verboseTooltips = verboseTooltips;
        changed();
    }

    public boolean darkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        changed();
    }

    public int fontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = Math.max(8, Math.min(24, fontSize));
        changed();
    }

    public int notificationDurationMs() {
        return notificationDurationMs;
    }

    public void setNotificationDurationMs(int notificationDurationMs) {
        this.notificationDurationMs = Math.max(0, notificationDurationMs);
        changed();
    }

    public int diagramWidth() {
        return diagramWidth;
    }

    public int diagramHeight() {
        return diagramHeight;
    }

    public void setDiagramSize(int width, int height) {
        this.diagramWidth = width;
        this.diagramHeight = height;
        changed();
    }

    public int zoomPercent() {
        return zoomPercent;
    }

    public void setZoomPercent(int zoomPercent) {
        this.zoomPercent = Math.max(25, Math.min(400, zoomPercent));
        changed();
    }
}
