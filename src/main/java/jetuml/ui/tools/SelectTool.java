package jetuml.ui.tools;

import jetuml.app.SnapService;
import jetuml.model.Edge;
import jetuml.model.Node;
import jetuml.model.Point;
import jetuml.model.Rect;
import jetuml.model.Vector;
import jetuml.model.nodes.PackageNode;
import jetuml.render.Java2DSurface;
import jetuml.render.Surface;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

public final class SelectTool implements Tool {
    private Point start;
    private Point current;
    private Node dragging;
    private Point dragOrigin;
    private boolean lasso;
    private Set<Node> original;

    @Override
    public void mousePressed(ToolContext c, MouseInput e) {
        start = e.point();
        current = e.point();
        Node node = c.renderer().hitNode(c.document().diagram(), e.point(), measure(c));
        Edge edge = node == null ? c.renderer().hitEdge(c.document().diagram(), e.point(), measure(c)) : null;
        if (e.ctrl() && node != null) {
            c.selection().toggle(node);
            dragging = null;
        } else if (node != null) {
            if (!c.selection().contains(node)) {
                c.selection().set(node);
            }
            dragging = node;
            dragOrigin = node.position();
            original = new HashSet<>(c.selection().nodes());
        } else if (edge != null) {
            c.selection().set(edge);
            dragging = null;
        } else {
            c.selection().clear();
            lasso = true;
            dragging = null;
        }
        c.repaint();
    }

    @Override
    public void mouseDragged(ToolContext c, MouseInput e) {
        current = e.point();
        if (dragging != null && original != null) {
            Vector delta = new Vector(e.point().x() - start.x(), e.point().y() - start.y());
            for (Node n : original) {
                Point dest = SnapService.snap(n.position().translate(delta.dx() - (n.position().x() - (n == dragging ? dragOrigin.x() : n.position().x())),
                        delta.dy() - (n.position().y() - (n == dragging ? dragOrigin.y() : n.position().y()))));
                // simplified: move relative to start stored in original positions via editor at release
            }
            c.repaint();
        } else if (lasso) {
            c.repaint();
        }
    }

    @Override
    public void mouseReleased(ToolContext c, MouseInput e) {
        if (dragging != null && original != null) {
            int dx = SnapService.snap(e.point().x() - start.x());
            int dy = SnapService.snap(e.point().y() - start.y());
            if (dx != 0 || dy != 0) {
                c.editor().moveNodes(original, new Vector(dx, dy));
            }
            if (e.shift()) {
                Node under = packageAt(c, e.point(), dragging);
                if (under instanceof PackageNode pkg && pkg != dragging) {
                    c.editor().attachToPackage(dragging, pkg, true);
                } else if (dragging.parent().orElse(null) instanceof PackageNode pkg) {
                    c.editor().attachToPackage(dragging, pkg, false);
                }
            }
        } else if (lasso) {
            Rect box = Rect.fromCorners(start, e.point());
            var g = measure(c);
            for (Node n : c.document().diagram().allNodes()) {
                if (box.intersects(c.renderer().boundsOf(new jetuml.model.NodeEndpoint(n), g))) {
                    c.selection().add(n);
                }
            }
        }
        lasso = false;
        dragging = null;
        original = null;
        c.repaint();
    }

    private Node packageAt(ToolContext c, Point p, Node ignore) {
        for (Node n : c.document().diagram().allNodes()) {
            if (n instanceof PackageNode && n != ignore && c.renderer().hitNode(c.document().diagram(), p, measure(c)) == n) {
                return n;
            }
        }
        Node hit = c.renderer().hitNode(c.document().diagram(), p, measure(c));
        return hit instanceof PackageNode ? hit : null;
    }

    @Override
    public void paintOverlay(Surface s) {
        if (!lasso || start == null || current == null) {
            return;
        }
        Rect r = Rect.fromCorners(start, current);
        if (s instanceof Java2DSurface j) {
            Graphics2D g = j.graphics();
            g.setColor(new Color(30, 136, 229, 50));
            g.fillRect(r.x(), r.y(), r.width(), r.height());
            g.setColor(new Color(30, 136, 229));
            g.drawRect(r.x(), r.y(), r.width(), r.height());
        } else {
            s.setColor(0x1E88E5);
            s.rect(r, false);
        }
    }

    private jetuml.render.Java2DSurface measure(ToolContext c) {
        return c.canvas().measureSurface();
    }
}
