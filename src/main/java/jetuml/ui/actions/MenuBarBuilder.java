package jetuml.ui.actions;

import jetuml.app.DiagramDocument;
import jetuml.app.ElementClipboard;
import jetuml.app.Preferences;
import jetuml.app.Workspace;
import jetuml.export.ClipboardImageService;
import jetuml.export.ImageExporter;
import jetuml.model.types.DiagramType;
import jetuml.model.types.DiagramTypeRegistry;
import jetuml.persistence.JsonDiagramRepository;
import jetuml.ui.DiagramCanvas;
import jetuml.ui.MainFrame;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;

/**
 * Builds the menu bar and registers all {@link AppAction}s into the {@link ActionRegistry}.
 * All actions are defined here once; the same AppAction objects are reused for menu items,
 * toolbar buttons, context menus, and keyboard shortcuts (RNF03).
 */
public final class MenuBarBuilder {

    private MenuBarBuilder() {}

    /**
     * Registers all application actions and returns a fully-wired {@link JMenuBar}.
     */
    public static JMenuBar build(MainFrame frame, Workspace workspace,
                                 ActionRegistry registry, ElementClipboard clipboard,
                                 JsonDiagramRepository repo) {

        registerFileActions(registry, frame, workspace, repo);
        registerEditActions(registry, frame, workspace, clipboard);
        registerViewActions(registry, frame);
        registerDiagramActions(registry, frame, workspace);
        registerHelpActions(registry, frame);

        JMenuBar bar = new JMenuBar();
        bar.add(buildFileMenu(registry));
        bar.add(buildEditMenu(registry));
        bar.add(buildViewMenu(registry));
        bar.add(buildDiagramMenu(registry));
        bar.add(buildHelpMenu(registry));
        return bar;
    }

    // ────────────────────────────────────────────────────────────────────
    // File actions
    // ────────────────────────────────────────────────────────────────────

    private static void registerFileActions(ActionRegistry r, MainFrame frame,
                                            Workspace workspace, JsonDiagramRepository repo) {
        for (DiagramType type : DiagramTypeRegistry.all()) {
            String id = "file.new." + type.id().toLowerCase();
            r.register(new AppAction(id, "New " + type.displayName() + " Diagram",
                    null, null, () -> {
                workspace.newDiagram(type);
                frame.syncTabs();
            }));
        }

        r.register(new AppAction("file.open", "Open…",
                KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), null, () -> {
            JFileChooser fc = createChooser(null, false);
            if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
            File file = fc.getSelectedFile();
            try {
                DiagramDocument doc = repo.load(file.toPath(), workspace.notifications());
                workspace.add(doc);
                frame.syncTabs();
            } catch (Exception ex) {
                workspace.notifications().error("Could not open file: " + ex.getMessage());
            }
        }));

        r.register(new AppAction("file.save", "Save",
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK),
                () -> workspace.active().isPresent(), () -> {
            workspace.active().ifPresent(doc -> {
                if (doc.path().isPresent()) {
                    saveSilently(repo, doc, workspace);
                } else {
                    saveAs(repo, doc, workspace, frame);
                }
            });
        }));

        r.register(new AppAction("file.saveAs", "Save As…",
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
                () -> workspace.active().isPresent(), () ->
                workspace.active().ifPresent(doc -> saveAs(repo, doc, workspace, frame))));

        r.register(new AppAction("file.close", "Close",
                KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK),
                () -> workspace.active().isPresent(), () ->
                workspace.active().ifPresent(doc -> {
                    if (confirmClose(doc, repo, workspace, frame)) {
                        workspace.close(doc);
                        frame.syncTabs();
                    }
                })));

        r.register(new AppAction("file.exportImage", "Export Image…",
                KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK),
                () -> workspace.active().isPresent(), () ->
                workspace.active().ifPresent(doc -> exportImage(doc, workspace, frame))));

        r.register(new AppAction("file.copyImage", "Copy Diagram to Clipboard",
                KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK),
                () -> workspace.active().isPresent(), () ->
                workspace.active().ifPresent(doc -> {
                    ClipboardImageService.copy(doc, workspace.notifications());
                })));

        r.register(new AppAction("file.exit", "Exit",
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), null, frame::exit));
    }

    private static void saveSilently(JsonDiagramRepository repo, DiagramDocument doc,
                                     Workspace workspace) {
        try {
            repo.save(doc, doc.path().get());
            workspace.notifications().info("Saved.");
        } catch (Exception ex) {
            workspace.notifications().error("Save failed: " + ex.getMessage());
        }
    }

    private static void saveAs(JsonDiagramRepository repo, DiagramDocument doc,
                               Workspace workspace, MainFrame frame) {
        JFileChooser fc = createChooser(
                doc.path().map(Path::toString).orElse(null), true);
        if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".jet")) {
            file = new File(file.getAbsolutePath() + ".jet");
        }
        try {
            repo.save(doc, file.toPath());
            workspace.notifications().info("Saved.");
            frame.syncTabs();
        } catch (Exception ex) {
            workspace.notifications().error("Save failed: " + ex.getMessage());
        }
    }

    private static boolean confirmClose(DiagramDocument doc, JsonDiagramRepository repo,
                                        Workspace workspace, MainFrame frame) {
        if (!doc.modified()) return true;
        int choice = JOptionPane.showConfirmDialog(frame,
                "Save changes to " + doc.title() + "?",
                "JetUML", JOptionPane.YES_NO_CANCEL_OPTION);
        if (choice == JOptionPane.CANCEL_OPTION) return false;
        if (choice == JOptionPane.YES_OPTION) {
            if (doc.path().isPresent()) {
                saveSilently(repo, doc, workspace);
            } else {
                saveAs(repo, doc, workspace, frame);
            }
        }
        return true;
    }

    private static void exportImage(DiagramDocument doc, Workspace workspace, MainFrame frame) {
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new FileNameExtensionFilter("SVG Image (*.svg)", "svg"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Image (*.jpg)", "jpg"));
        if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        try {
            ImageExporter.forPath(file.toPath()).export(doc, file.toPath());
        } catch (Exception ex) {
            workspace.notifications().error("Export failed: " + ex.getMessage());
        }
    }

    private static JFileChooser createChooser(String existingPath, boolean save) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("JetUML Diagram (*.jet)", "jet"));
        if (existingPath != null) {
            fc.setSelectedFile(new File(existingPath));
        }
        return fc;
    }

    // ────────────────────────────────────────────────────────────────────
    // Edit actions
    // ────────────────────────────────────────────────────────────────────

    private static void registerEditActions(ActionRegistry r, MainFrame frame,
                                            Workspace workspace, ElementClipboard clipboard) {
        r.register(new AppAction("edit.undo", "Undo",
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK),
                () -> workspace.active().map(d -> d.undo().canUndo()).orElse(false), () ->
                workspace.active().ifPresent(d -> {
                    d.editor().undo();
                    frame.repaintActive();
                })));

        r.register(new AppAction("edit.redo", "Redo",
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK),
                () -> workspace.active().map(d -> d.undo().canRedo()).orElse(false), () ->
                workspace.active().ifPresent(d -> {
                    d.editor().redo();
                    frame.repaintActive();
                })));

        r.register(new AppAction("edit.selectAll", "Select All",
                KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK),
                () -> workspace.active().isPresent(), () ->
                workspace.active().ifPresent(d -> {
                    d.selection().addAll(d.diagram().allNodes(), d.diagram().edges());
                    frame.repaintActive();
                })));

        r.register(new AppAction("edit.delete", "Delete",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                () -> workspace.active().map(d -> !d.selection().isEmpty()).orElse(false), () ->
                workspace.active().ifPresent(d -> {
                    d.editor().deleteSelection(d.selection());
                    frame.repaintActive();
                })));

        r.register(new AppAction("edit.copy", "Copy",
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK),
                () -> workspace.active().map(d -> !d.selection().isEmpty()).orElse(false), () ->
                workspace.active().ifPresent(d -> {
                    clipboard.copy(d.selection(), d.diagram());
                })));

        r.register(new AppAction("edit.paste", "Paste",
                KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK),
                () -> workspace.active().isPresent() && !clipboard.isEmpty(), () ->
                workspace.active().ifPresent(d -> {
                    d.editor().paste(clipboard.content(), new jetuml.model.Point(20, 20));
                    frame.repaintActive();
                })));

        r.register(new AppAction("edit.properties", "Properties…",
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),
                () -> workspace.active().map(d -> d.selection().hasSingleElement()).orElse(false),
                () -> workspace.active().ifPresent(d -> frame.openPropertiesForSelection(d))));
    }

    // ────────────────────────────────────────────────────────────────────
    // View actions
    // ────────────────────────────────────────────────────────────────────

    private static void registerViewActions(ActionRegistry r, MainFrame frame) {
        r.register(new AppAction("view.grid", "Show Grid",
                KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK),
                null, () -> {
            Preferences.INSTANCE.setShowGrid(!Preferences.INSTANCE.showGrid());
            frame.repaintActive();
        }));

        r.register(new AppAction("view.darkMode", "Dark Mode",
                null, null, () -> {
            Preferences.INSTANCE.setDarkMode(!Preferences.INSTANCE.darkMode());
            frame.repaintAll();
        }));

        r.register(new AppAction("view.zoomIn", "Zoom In",
                KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK), null, () -> {
            Preferences.INSTANCE.setZoomPercent(Preferences.INSTANCE.zoomPercent() + 10);
        }));

        r.register(new AppAction("view.zoomOut", "Zoom Out",
                KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), null, () -> {
            Preferences.INSTANCE.setZoomPercent(Preferences.INSTANCE.zoomPercent() - 10);
        }));

        r.register(new AppAction("view.zoomReset", "Reset Zoom",
                KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK),
                null, () -> Preferences.INSTANCE.setZoomPercent(100)));
    }

    // ────────────────────────────────────────────────────────────────────
    // Diagram actions
    // ────────────────────────────────────────────────────────────────────

    private static void registerDiagramActions(ActionRegistry r, MainFrame frame, Workspace workspace) {
        r.register(new AppAction("diagram.preferences", "Diagram Preferences…",
                null, null, () -> frame.openPreferences()));
    }

    // ────────────────────────────────────────────────────────────────────
    // Help actions
    // ────────────────────────────────────────────────────────────────────

    private static void registerHelpActions(ActionRegistry r, MainFrame frame) {
        r.register(new AppAction("help.about", "About JetUML…",
                null, null, () -> JOptionPane.showMessageDialog(frame,
                "JetUML\nA lightweight UML diagram editor\n\nBuilt with Java 21 + Swing",
                "About", JOptionPane.INFORMATION_MESSAGE)));
    }

    // ────────────────────────────────────────────────────────────────────
    // Menu construction
    // ────────────────────────────────────────────────────────────────────

    private static JMenu buildFileMenu(ActionRegistry r) {
        JMenu m = new JMenu("File");
        m.setMnemonic(KeyEvent.VK_F);
        for (DiagramType type : DiagramTypeRegistry.all()) {
            m.add(item(r, "file.new." + type.id().toLowerCase()));
        }
        m.addSeparator();
        m.add(item(r, "file.open"));
        m.addSeparator();
        m.add(item(r, "file.save"));
        m.add(item(r, "file.saveAs"));
        m.addSeparator();
        m.add(item(r, "file.close"));
        m.addSeparator();
        m.add(item(r, "file.exportImage"));
        m.add(item(r, "file.copyImage"));
        m.addSeparator();
        m.add(item(r, "file.exit"));
        return m;
    }

    private static JMenu buildEditMenu(ActionRegistry r) {
        JMenu m = new JMenu("Edit");
        m.setMnemonic(KeyEvent.VK_E);
        m.add(item(r, "edit.undo"));
        m.add(item(r, "edit.redo"));
        m.addSeparator();
        m.add(item(r, "edit.selectAll"));
        m.add(item(r, "edit.delete"));
        m.addSeparator();
        m.add(item(r, "edit.copy"));
        m.add(item(r, "edit.paste"));
        m.addSeparator();
        m.add(item(r, "edit.properties"));
        return m;
    }

    private static JMenu buildViewMenu(ActionRegistry r) {
        JMenu m = new JMenu("View");
        m.setMnemonic(KeyEvent.VK_V);
        m.add(item(r, "view.grid"));
        m.add(item(r, "view.darkMode"));
        m.addSeparator();
        m.add(item(r, "view.zoomIn"));
        m.add(item(r, "view.zoomOut"));
        m.add(item(r, "view.zoomReset"));
        return m;
    }

    private static JMenu buildDiagramMenu(ActionRegistry r) {
        JMenu m = new JMenu("Diagram");
        m.setMnemonic(KeyEvent.VK_D);
        m.add(item(r, "diagram.preferences"));
        return m;
    }

    private static JMenu buildHelpMenu(ActionRegistry r) {
        JMenu m = new JMenu("Help");
        m.setMnemonic(KeyEvent.VK_H);
        m.add(item(r, "help.about"));
        return m;
    }

    private static JMenuItem item(ActionRegistry r, String id) {
        return new JMenuItem(r.swing(id));
    }
}
