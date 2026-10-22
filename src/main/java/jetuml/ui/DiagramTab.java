package jetuml.ui;

import jetuml.app.DiagramDocument;
import jetuml.app.ElementClipboard;
import jetuml.app.Workspace;
import jetuml.model.types.ToolSpec;
import jetuml.ui.actions.ActionRegistry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A single tab in the main window's tabbed pane.
 * Contains a {@link ToolbarPanel} on the left and the {@link DiagramCanvas} in the center.
 */
public final class DiagramTab extends JPanel {

    private final DiagramDocument document;
    private final DiagramCanvas canvas;
    private final ToolbarPanel toolbar;

    public DiagramTab(DiagramDocument document, MainFrame ownerFrame,
                      ActionRegistry registry, ElementClipboard clipboard, Workspace workspace) {
        this.document = document;
        this.canvas = new DiagramCanvas(document, ownerFrame);

        this.toolbar = new ToolbarPanel(document.diagram().type().tools(), spec -> onToolSelected(spec));

        setLayout(new BorderLayout());
        add(toolbar.asPanel(), BorderLayout.WEST);

        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Right-click context menu
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    ContextMenuBuilder.show(e, canvas, document, registry, clipboard, workspace, ownerFrame);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    ContextMenuBuilder.show(e, canvas, document, registry, clipboard, workspace, ownerFrame);
                }
            }
        });
    }

    private void onToolSelected(ToolSpec spec) {
        canvas.setTool(spec);
        canvas.requestFocusInWindow();
    }

    public DiagramDocument document() {
        return document;
    }

    public DiagramCanvas canvas() {
        return canvas;
    }

    public String tabTitle() {
        return document.title();
    }

    public void repaintCanvas() {
        canvas.repaint();
    }
}
