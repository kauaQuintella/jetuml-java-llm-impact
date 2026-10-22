package jetuml.render;

import jetuml.app.SelectionModel;
import jetuml.model.DiagramView;
import jetuml.model.Edge;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.Point;
import jetuml.model.PointEndpoint;
import jetuml.model.Rect;

import java.util.HashMap;
import java.util.Map;

public final class DiagramRenderer {
    public void draw(DiagramView diagram, Surface surface, Theme theme, boolean grid, Rect canvas, SelectionModel selection) {
        surface.setColor(theme.canvas());
        surface.rect(canvas, true);
        if (grid) {
            Grid.draw(surface, canvas, theme);
        }
        Map<Edge, EdgePath> paths = paths(diagram, surface);
        for (Node node : diagram.allNodes()) {
            if (node.parent().isPresent()) {
                continue;
            }
            drawTree(node, surface, theme, selection);
        }
        for (Node node : diagram.allNodes()) {
            if (node.parent().isPresent()) {
                RendererRegistry.draw(node, surface, theme);
                highlight(node, surface, theme, selection);
            }
        }
        for (Map.Entry<Edge, EdgePath> e : paths.entrySet()) {
            EdgeRenderer.draw(e.getKey(), e.getValue(), surface, theme);
            if (selection != null && selection.contains(e.getKey())) {
                surface.setColor(theme.selected());
                for (Point p : e.getValue().points()) {
                    surface.rect(new Rect(p.x() - 3, p.y() - 3, 6, 6), true);
                }
            }
        }
    }

    private void drawTree(Node node, Surface surface, Theme theme, SelectionModel selection) {
        RendererRegistry.draw(node, surface, theme);
        highlight(node, surface, theme, selection);
        for (Node child : node.children()) {
            if (node.getClass().getSimpleName().equals("PackageNode")) {
                drawTree(child, surface, theme, selection);
            }
        }
    }

    private void highlight(Node node, Surface surface, Theme theme, SelectionModel selection) {
        if (selection != null && selection.contains(node)) {
            Rect b = RendererRegistry.bounds(node, surface);
            surface.setColor(theme.selected());
            surface.rect(new Rect(b.x() - 2, b.y() - 2, b.width() + 4, b.height() + 4), false);
        }
    }

    public Map<Edge, EdgePath> paths(DiagramView diagram, Surface surface) {
        Map<Edge, EdgePath> map = new HashMap<>();
        Map<Node, Integer> selfCount = new HashMap<>();
        for (Edge edge : diagram.edges()) {
            Rect from = boundsOf(edge.start(), surface);
            Rect to = boundsOf(edge.end(), surface);
            int idx = 0;
            if (edge.start() instanceof NodeEndpoint a && edge.end() instanceof NodeEndpoint b && a.node() == b.node()) {
                idx = selfCount.merge(a.node(), 1, Integer::sum) - 1;
            }
            map.put(edge, EdgeRenderer.path(edge, from, to, idx));
        }
        return map;
    }

    public Rect boundsOf(jetuml.model.Endpoint endpoint, Surface surface) {
        if (endpoint instanceof NodeEndpoint ne) {
            return RendererRegistry.bounds(ne.node(), surface);
        }
        Point p = ((PointEndpoint) endpoint).point();
        return new Rect(p.x() - 2, p.y() - 2, 4, 4);
    }

    public Rect contentBounds(DiagramView diagram, Surface surface) {
        Rect bounds = new Rect(0, 0, 1, 1);
        boolean any = false;
        for (Node n : diagram.allNodes()) {
            Rect b = RendererRegistry.bounds(n, surface);
            bounds = any ? bounds.union(b) : b;
            any = true;
        }
        return any ? bounds : new Rect(0, 0, 100, 100);
    }

    public Node hitNode(DiagramView diagram, Point p, Surface surface) {
        var all = diagram.allNodes();
        for (int i = all.size() - 1; i >= 0; i--) {
            Node n = all.get(i);
            if (RendererRegistry.of(n).contains(n, p, surface)) {
                return n;
            }
        }
        return null;
    }

    public Edge hitEdge(DiagramView diagram, Point p, Surface surface) {
        Map<Edge, EdgePath> paths = paths(diagram, surface);
        for (Map.Entry<Edge, EdgePath> e : paths.entrySet()) {
            if (EdgeRenderer.hit(e.getValue(), p, 6)) {
                return e.getKey();
            }
        }
        return null;
    }
}
