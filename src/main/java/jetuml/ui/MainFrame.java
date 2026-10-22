package jetuml.ui;

import jetuml.app.DiagramDocument;
import jetuml.app.ElementClipboard;
import jetuml.app.Preferences;
import jetuml.app.Workspace;
import jetuml.model.Element;
import jetuml.persistence.JsonDiagramRepository;
import jetuml.ui.actions.ActionRegistry;
import jetuml.ui.actions.MenuBarBuilder;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * The application's main window.
 * Manages a {@link JTabbedPane} of {@link DiagramTab}s, the menu bar,
 * the notification overlay, and the welcome panel shown when no diagram is open.
 */
public final class MainFrame extends JFrame {

    private final Workspace workspace;
    private final ActionRegistry registry = new ActionRegistry();
    private final ElementClipboard clipboard = new ElementClipboard();
    private final JsonDiagramRepository repo = new JsonDiagramRepository();
    private final JTabbedPane tabs = new JTabbedPane();
    private final NotificationOverlay overlay;
    private final Map<DiagramDocument, DiagramTab> tabMap = new HashMap<>();
    private final WelcomePanel welcomePanel;

    public MainFrame(Workspace workspace) {
        super("JetUML");
        this.workspace = workspace;
        this.overlay = new NotificationOverlay(workspace.notifications());

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        // Build menu bar (also registers all actions)
        setJMenuBar(MenuBarBuilder.build(this, workspace, registry, clipboard, repo));

        // Root content
        JPanel root = new JPanel(new BorderLayout());
        welcomePanel = new WelcomePanel(workspace, type -> {
            workspace.newDiagram(type);
            syncTabs();
        });

        root.add(tabs, BorderLayout.CENTER);
        root.add(overlay, BorderLayout.SOUTH);
        setContentPane(root);

        // Listen for workspace changes
        workspace.subscribe(ws -> SwingUtilities.invokeLater(this::syncTabs));

        // Keyboard shortcuts bound globally
        registry.bindAll(root);

        // Tab change → update active document
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx >= 0) {
                Component comp = tabs.getComponentAt(idx);
                tabMap.values().stream()
                        .filter(t -> t == comp)
                        .findFirst()
                        .ifPresent(t -> workspace.setActive(t.document()));
            }
        });

        setMinimumSize(new Dimension(800, 600));
        setSize(1200, 800);
        setLocationRelativeTo(null);

        syncTabs();
    }

    /**
     * Synchronises the tab pane with the current workspace state.
     * Called after any document open/close/create.
     */
    public void syncTabs() {
        // Remove tabs for closed documents
        tabMap.entrySet().removeIf(entry -> {
            if (!workspace.documents().contains(entry.getKey())) {
                tabs.remove(entry.getValue());
                return true;
            }
            return false;
        });

        // Add tabs for new documents
        for (DiagramDocument doc : workspace.documents()) {
            if (!tabMap.containsKey(doc)) {
                DiagramTab tab = new DiagramTab(doc, this, registry, clipboard, workspace);
                tabMap.put(doc, tab);
                tabs.addTab(doc.title(), tab);
            } else {
                // Refresh tab title in case modified flag changed
                int idx = tabs.indexOfComponent(tabMap.get(doc));
                if (idx >= 0) {
                    tabs.setTitleAt(idx, doc.title());
                }
            }
        }

        // Show welcome panel when no documents exist
        if (workspace.documents().isEmpty()) {
            if (tabs.getTabCount() == 0) {
                tabs.addTab("Welcome", welcomePanel);
            }
        } else {
            // Remove welcome tab if present
            int welcomeIdx = tabs.indexOfComponent(welcomePanel);
            if (welcomeIdx >= 0) {
                tabs.removeTabAt(welcomeIdx);
            }
            // Select the active document's tab
            workspace.active().ifPresent(doc -> {
                DiagramTab tab = tabMap.get(doc);
                if (tab != null) {
                    tabs.setSelectedComponent(tab);
                }
            });
        }

        // Update frame title
        setTitle(workspace.active()
                .map(d -> d.title() + " — JetUML")
                .orElse("JetUML"));
    }

    /** Repaints the canvas of the currently active tab. */
    public void repaintActive() {
        workspace.active()
                .map(tabMap::get)
                .ifPresent(DiagramTab::repaintCanvas);
    }

    /** Repaints all open canvases (used on theme change). */
    public void repaintAll() {
        tabMap.values().forEach(DiagramTab::repaintCanvas);
    }

    /** Opens the properties dialog for the selection in the given document. */
    public void openPropertiesForSelection(DiagramDocument doc) {
        doc.selection().single().ifPresent(element ->
                openProperties(element, doc));
    }

    /** Opens the properties dialog for a specific element. */
    public void openProperties(Element element, DiagramDocument doc) {
        PropertiesDialog dialog = new PropertiesDialog(this, element, doc.editor());
        dialog.setVisible(true);
        repaintActive();
    }

    /** Opens the application preferences dialog. */
    public void openPreferences() {
        new PreferencesDialog(this).setVisible(true);
    }

    /**
     * Attempts to close all documents and exit.
     * Prompts to save modified documents.
     */
    public void exit() {
        for (DiagramDocument doc : workspace.documents()) {
            workspace.setActive(doc);
            if (doc.modified()) {
                // Try to close with save prompt via registry action
                registry.get("file.close").run().run();
                if (workspace.documents().contains(doc)) {
                    return; // user cancelled
                }
            }
        }
        System.exit(0);
    }
}
