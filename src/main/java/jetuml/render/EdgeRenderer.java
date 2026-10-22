package jetuml.render;

import jetuml.model.Edge;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.Rect;
import jetuml.model.edges.EdgeType;

import java.util.List;

public final class EdgeRenderer {
    private EdgeRenderer() {
    }

    public static EdgePath path(Edge edge, Rect from, Rect to, int selfIndex) {
        if (from.equals(to) || (edge.start() instanceof jetuml.model.NodeEndpoint a
                && edge.end() instanceof jetuml.model.NodeEndpoint b
                && a.node() == b.node())) {
            return new SelfLoopPath(selfIndex).path(from, to);
        }
        if (edge.type() == EdgeType.CALL || edge.type() == EdgeType.RETURN) {
            return new SegmentedPath().path(from, to);
        }
        return new StraightPath().path(from, to);
    }

    public static void draw(Edge edge, EdgePath path, Surface surface, Theme theme) {
        surface.setColor(theme.ink());
        surface.dashed(edge.type().dashed());
        List<Point> pts = path.points();
        for (int i = 0; i < pts.size() - 1; i++) {
            surface.line(pts.get(i), pts.get(i + 1));
        }
        surface.dashed(false);
        drawHead(edge, pts.get(pts.size() - 2), pts.getLast(), surface, theme);
        if (edge.type().descriptors().stream().anyMatch(d -> d.key().equals("directionality"))) {
            String dir = String.valueOf(edge.properties().getRaw("directionality"));
            if ("Bidirectional".equals(dir)) {
                drawArrow(pts.get(1), pts.getFirst(), surface, false, false);
            }
        }
        drawLabels(edge, path, surface, theme);
    }

    private static void drawHead(Edge edge, Point from, Point to, Surface surface, Theme theme) {
        surface.setColor(theme.ink());
        switch (edge.type()) {
            case INHERITANCE, GENERALIZATION, REALIZATION -> drawTriangle(from, to, surface);
            case AGGREGATION -> drawDiamond(from, to, surface, false);
            case COMPOSITION -> drawDiamond(from, to, surface, true);
            case ASSOCIATION, USECASE_ASSOCIATION -> {
                String dir = String.valueOf(edge.properties().getRaw("directionality"));
                if ("Unidirectional".equals(dir) || "Bidirectional".equals(dir)
                        || edge.type() == EdgeType.ASSOCIATION && dir.equals("Unidirectional")) {
                    if (!"None".equals(dir) && !"null".equals(dir)) {
                        drawArrow(from, to, surface, false, false);
                    }
                }
            }
            case DEPENDENCY, INCLUDE, EXTEND, CALL, RETURN, REFERENCE, NOTE_CONNECTOR, TRANSITION, COLLABORATION ->
                    drawArrow(from, to, surface, false, edge.type() == EdgeType.TRANSITION);
            default -> {
            }
        }
        Object agg = edge.properties().getRaw("aggregationType");
        if (agg != null && !"None".equals(String.valueOf(agg)) && edge.type() == EdgeType.ASSOCIATION) {
            drawDiamond(from, to, surface, "Composition".equals(String.valueOf(agg)));
        }
    }

    private static void drawLabels(Edge edge, EdgePath path, Surface surface, Theme theme) {
        TextStyle st = TextStyle.regular(theme.fontSize() - 1);
        surface.setColor(theme.ink());
        put(edge, "startLabel", path.start(), surface, st, -18, -4);
        put(edge, "middleLabel", path.middle(), surface, st, 6, -6);
        put(edge, "endLabel", path.end(), surface, st, -18, 12);
    }

    private static void put(Edge edge, String key, Point at, Surface surface, TextStyle st, int dx, int dy) {
        if (edge.descriptors().stream().noneMatch(d -> d.key().equals(key))) {
            return;
        }
        String v = edge.get(new Property<>(key, String.class, ""));
        if (v == null || v.isBlank()) {
            return;
        }
        surface.text(v, at.translate(dx, dy), st);
    }

    private static void drawArrow(Point from, Point to, Surface surface, boolean filled, boolean open) {
        double angle = Math.atan2(to.y() - from.y(), to.x() - from.x());
        int s = 10;
        Point p1 = new Point(to.x() - (int) (s * Math.cos(angle - 0.4)), to.y() - (int) (s * Math.sin(angle - 0.4)));
        Point p2 = new Point(to.x() - (int) (s * Math.cos(angle + 0.4)), to.y() - (int) (s * Math.sin(angle + 0.4)));
        if (filled) {
            surface.polygon(List.of(to, p1, p2), true);
        } else {
            surface.line(to, p1);
            surface.line(to, p2);
        }
    }

    private static void drawTriangle(Point from, Point to, Surface surface) {
        double angle = Math.atan2(to.y() - from.y(), to.x() - from.x());
        int s = 12;
        Point p1 = new Point(to.x() - (int) (s * Math.cos(angle - 0.45)), to.y() - (int) (s * Math.sin(angle - 0.45)));
        Point p2 = new Point(to.x() - (int) (s * Math.cos(angle + 0.45)), to.y() - (int) (s * Math.sin(angle + 0.45)));
        surface.polygon(List.of(to, p1, p2), false);
    }

    private static void drawDiamond(Point from, Point to, Surface surface, boolean filled) {
        double angle = Math.atan2(to.y() - from.y(), to.x() - from.x());
        int s = 12;
        Point tip = to;
        Point left = new Point(to.x() - (int) (s * Math.cos(angle - 0.5)), to.y() - (int) (s * Math.sin(angle - 0.5)));
        Point mid = new Point(to.x() - (int) (1.6 * s * Math.cos(angle)), to.y() - (int) (1.6 * s * Math.sin(angle)));
        Point right = new Point(to.x() - (int) (s * Math.cos(angle + 0.5)), to.y() - (int) (s * Math.sin(angle + 0.5)));
        surface.polygon(List.of(tip, left, mid, right), filled);
    }

    public static boolean hit(EdgePath path, Point p, int tolerance) {
        List<Point> pts = path.points();
        for (int i = 0; i < pts.size() - 1; i++) {
            if (distToSegment(p, pts.get(i), pts.get(i + 1)) <= tolerance) {
                return true;
            }
        }
        return false;
    }

    private static double distToSegment(Point p, Point a, Point b) {
        double dx = b.x() - a.x();
        double dy = b.y() - a.y();
        if (dx == 0 && dy == 0) {
            return Math.hypot(p.x() - a.x(), p.y() - a.y());
        }
        double t = ((p.x() - a.x()) * dx + (p.y() - a.y()) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        return Math.hypot(p.x() - (a.x() + t * dx), p.y() - (a.y() + t * dy));
    }
}
