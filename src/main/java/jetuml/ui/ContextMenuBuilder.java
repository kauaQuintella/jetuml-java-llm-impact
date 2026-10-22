package jetuml.ui;

import jetuml.app.DiagramDocument;
import jetuml.app.ElementClipboard;
import jetuml.app.Workspace;
import jetuml.model.Edge;
import jetuml.model.Element;
import jetuml.model.Node;
import jetuml.model.Point;
import jetuml.render.DiagramRenderer;
import jetuml.render.Java2DSurface;
import jetuml.ui.actions.ActionRegistry;
import jetuml.ui.actions.AppAction;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.MouseEvent;

/**
 * Builds the right-click context menu for the diagram canvas.
 * Reuses the same {@link AppAction} objects registered in {@link ActionRegistry}.
 */
public final class ContextMenuBuilder {

    private ContextMenuBuilder() {}

    /**
     * Creates and shows a context menu at the given mouse event position.
     */
    public static void show(MouseEvent e, DiagramCanvas canvas, DiagramDocument doc,
                            ActionRegistry registry, ElementClipboard clipboard,
                            Workspace workspace, MainFrame frame) {
        Point p = new Point(e.getX(), e.getY());
        DiagramRenderer renderer = new DiagramRenderer();
        Java2DSurface surface = canvas.measureSurface();

        Node hitNode = renderer.hitNode(doc.diagram(), p, surface);
        Edge hitEdge = (hitNode == null) ? renderer.hitEdge(doc.diagram(), p, surface) : null;

        if (hitNode != null && !doc.selection().contains(hitNode)) {
            doc.selection().set(hitNode);
            canvas.repaint();
        } else if (hitEdge != null && !doc.selection().contains(hitEdge)) {
            doc.selection().set(hitEdge);
            canvas.repaint();
        }

        JPopupMenu menu = new JPopupMenu();

        if (!doc.selection().isEmpty()) {
            menu.add(item(registry, "edit.delete"));
            menu.add(item(registry, "edit.copy"));
        }

        if (!clipboard.isEmpty() && doc.diagram().type().allowsPaste()) {
            menu.add(item(registry, "edit.paste"));
        }

        menu.addSeparator();
        menu.add(item(registry, "edit.undo"));
        menu.add(item(registry, "edit.redo"));

        Element single = singleElement(doc);
        if (single != null) {
            menu.addSeparator();
            JMenuItem props = new JMenuItem("Properties…");
            props.addActionListener(ev -> frame.openPropertiesForSelection(doc));
            menu.add(props);
        }

        menu.show(canvas, e.getX(), e.getY());
    }

    private static Element singleElement(DiagramDocument doc) {
        if (doc.selection().nodes().size() == 1 && doc.selection().edges().isEmpty()) {
            return doc.selection().nodes().iterator().next();
        }
        if (doc.selection().edges().size() == 1 && doc.selection().nodes().isEmpty()) {
            return doc.selection().edges().iterator().next();
        }
        return null;
    }

    private static JMenuItem item(ActionRegistry r, String id) {
        return new JMenuItem(r.swing(id));
    }
}
