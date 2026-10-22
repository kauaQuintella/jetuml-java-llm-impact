package jetuml.render;

import jetuml.model.Point;
import jetuml.model.Rect;

import java.util.List;

public final class StraightPath implements EdgePathStrategy {
    @Override
    public EdgePath path(Rect from, Rect to) {
        Point a = attach(from, to.center());
        Point b = attach(to, from.center());
        return new EdgePath(List.of(a, b));
    }

    static Point attach(Rect box, Point towards) {
        Point c = box.center();
        int dx = towards.x() - c.x();
        int dy = towards.y() - c.y();
        if (dx == 0 && dy == 0) {
            return c;
        }
        double sx = dx == 0 ? Double.POSITIVE_INFINITY : (box.width() / 2.0) / Math.abs(dx);
        double sy = dy == 0 ? Double.POSITIVE_INFINITY : (box.height() / 2.0) / Math.abs(dy);
        double s = Math.min(sx, sy);
        return new Point(c.x() + (int) (dx * s), c.y() + (int) (dy * s));
    }
}
