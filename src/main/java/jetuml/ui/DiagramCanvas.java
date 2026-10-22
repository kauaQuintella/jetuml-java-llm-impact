package jetuml.ui;

import jetuml.app.DiagramDocument;
import jetuml.app.Preferences;
import jetuml.model.Edge;
import jetuml.model.Element;
import jetuml.model.Node;
import jetuml.model.Point;
import jetuml.render.DiagramRenderer;
import jetuml.render.Java2DSurface;
import jetuml.render.Surface;
import jetuml.render.Theme;
import jetuml.ui.tools.EdgeCreationTool;
import jetuml.ui.tools.MouseInput;
import jetuml.ui.tools.NodeCreationTool;
import jetuml.ui.tools.SelectTool;
import jetuml.ui.tools.Tool;
import jetuml.ui.tools.ToolContext;
import jetuml.model.types.ToolSpec;
import jetuml.model.types.ToolKind;
import jetuml.model.ElementKind;
import jetuml.model.edges.EdgeType;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * The canvas on which a single diagram is drawn and edited.
 * Delegates all mouse interactions to the currently active {@link Tool}.
 */
public final class DiagramCanvas extends JPanel {

    private final DiagramDocument document;
    private final DiagramRenderer renderer = new DiagramRenderer();
    private Tool activeTool;
    private ToolContext toolContext;
    private JFrame ownerFrame;

    public DiagramCanvas(DiagramDocument document, JFrame ownerFrame) {
        this.document = document;
        this.ownerFrame = ownerFrame;
        this.activeTool = new SelectTool();
        this.toolContext = new ToolContext(document, this, renderer);

        setPreferredSize(new Dimension(document.width(), document.height()));
        setBackground(Color.WHITE);

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                activeTool.mousePressed(toolContext, toInput(e));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                activeTool.mouseDragged(toolContext, toInput(e));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                activeTool.mouseReleased(toolContext, toInput(e));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick(e);
                }
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    private MouseInput toInput(MouseEvent e) {
        boolean ctrl = (e.getModifiersEx() & java.awt.event.InputEvent.CTRL_DOWN_MASK) != 0;
        boolean shift = (e.getModifiersEx() & java.awt.event.InputEvent.SHIFT_DOWN_MASK) != 0;
        return new MouseInput(new Point(e.getX(), e.getY()), ctrl, shift, e.getButton(), e.getClickCount());
    }

    private void handleDoubleClick(MouseEvent e) {
        Point p = new Point(e.getX(), e.getY());
        Java2DSurface measure = measureSurface();
        Node node = renderer.hitNode(document.diagram(), p, measure);
        if (node != null) {
            openProperties(node);
            return;
        }
        Edge edge = renderer.hitEdge(document.diagram(), p, measure);
        if (edge != null) {
            openProperties(edge);
        }
    }

    /** Opens the properties dialog for a given element (Node or Edge). */
    public void openProperties(Element element) {
        PropertiesDialog dialog = new PropertiesDialog(ownerFrame, element, document.editor());
        dialog.setVisible(true);
        repaint();
    }

    /** Switches the active tool back to the SelectTool. */
    public void selectSelectTool() {
        activeTool = new SelectTool();
    }

    /** Activates the tool described by the given ToolSpec. */
    public void setTool(ToolSpec spec) {
        activeTool = switch (spec.kind()) {
            case SELECT -> new SelectTool();
            case CREATE_NODE -> new NodeCreationTool(spec.element());
            case CREATE_EDGE -> new EdgeCreationTool(edgeTypeFor(spec.element()));
            case ACTION -> activeTool; // ActionTool fires immediately; no mode change here
        };
    }

    private EdgeType edgeTypeFor(ElementKind kind) {
        return switch (kind) {
            case ASSOCIATION         -> EdgeType.ASSOCIATION;
            case AGGREGATION         -> EdgeType.AGGREGATION;
            case COMPOSITION         -> EdgeType.COMPOSITION;
            case INHERITANCE         -> EdgeType.INHERITANCE;
            case REALIZATION         -> EdgeType.REALIZATION;
            case DEPENDENCY          -> EdgeType.DEPENDENCY;
            case CALL                -> EdgeType.CALL;
            case RETURN              -> EdgeType.RETURN;
            case COLLABORATION       -> EdgeType.COLLABORATION;
            case REFERENCE           -> EdgeType.REFERENCE;
            case TRANSITION          -> EdgeType.TRANSITION;
            case NOTE_CONNECTOR      -> EdgeType.NOTE_CONNECTOR;
            case INCLUDE             -> EdgeType.INCLUDE;
            case EXTEND              -> EdgeType.EXTEND;
            case USECASE_ASSOCIATION -> EdgeType.USECASE_ASSOCIATION;
            case GENERALIZATION      -> EdgeType.GENERALIZATION;
            default                  -> EdgeType.ASSOCIATION;
        };
    }

    /**
     * Returns a Graphics2D-backed Surface suitable for measuring text sizes,
     * used by tools that need hit-testing without painting.
     */
    public Java2DSurface measureSurface() {
        Graphics2D g = (Graphics2D) getGraphics();
        if (g == null) {
            // Fall back to a buffered image if not yet realized
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1, 1,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            g = img.createGraphics();
        }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return new Java2DSurface(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme theme = Preferences.INSTANCE.darkMode()
                ? Theme.dark(Preferences.INSTANCE.fontSize())
                : Theme.light(Preferences.INSTANCE.fontSize());
        Java2DSurface surface = new Java2DSurface(g2);
        jetuml.model.Rect canvas = new jetuml.model.Rect(0, 0, document.width(), document.height());
        renderer.draw(document.diagram(), surface, theme,
                Preferences.INSTANCE.showGrid(), canvas, document.selection());
        activeTool.paintOverlay(surface);
        g2.dispose();
    }
}
