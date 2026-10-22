package jetuml.ui.tools;

import jetuml.model.Endpoint;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.Point;
import jetuml.model.PointEndpoint;
import jetuml.model.edges.EdgeType;
import jetuml.model.nodes.ImplicitParameterNode;
import jetuml.render.Surface;

public final class EdgeCreationTool implements Tool {
    private final EdgeType type;
    private Endpoint start;
    private Point rubber;

    public EdgeCreationTool(EdgeType type) {
        this.type = type;
    }

    public EdgeType type() {
        return type;
    }

    @Override
    public void mousePressed(ToolContext c, MouseInput e) {
        start = endpointAt(c, e.point());
        rubber = e.point();
    }

    @Override
    public void mouseDragged(ToolContext c, MouseInput e) {
        rubber = e.point();
        c.repaint();
    }

    @Override
    public void mouseReleased(ToolContext c, MouseInput e) {
        if (start == null) {
            return;
        }
        Endpoint end = endpointAt(c, e.point());
        if (end != null) {
            c.editor().addEdge(type, start, end);
        }
        start = null;
        rubber = null;
        c.requestSelectTool();
        c.repaint();
    }

    @Override
    public void paintOverlay(Surface s) {
        if (start == null || rubber == null) {
            return;
        }
        s.setColor(0x1E88E5);
        s.dashed(true);
        s.line(start.position(), rubber);
        s.dashed(false);
    }

    private Endpoint endpointAt(ToolContext c, Point p) {
        Node node = c.renderer().hitNode(c.document().diagram(), p, c.canvas().measureSurface());
        if (node != null) {
            return new NodeEndpoint(node);
        }
        if (type == EdgeType.NOTE_CONNECTOR) {
            return new PointEndpoint(p);
        }
        if (type == EdgeType.CALL || type == EdgeType.RETURN) {
            for (Node n : c.document().diagram().allNodes()) {
                if (n instanceof ImplicitParameterNode ip) {
                    var b = c.renderer().boundsOf(new NodeEndpoint(n), c.canvas().measureSurface());
                    if (Math.abs(p.x() - b.center().x()) < 16 && p.y() >= b.y()) {
                        return new NodeEndpoint(ip);
                    }
                }
            }
        }
        return null;
    }
}
