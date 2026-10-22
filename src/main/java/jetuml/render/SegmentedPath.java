package jetuml.render;

import jetuml.model.Point;
import jetuml.model.Rect;

import java.util.List;

public final class SegmentedPath implements EdgePathStrategy {
    @Override
    public EdgePath path(Rect from, Rect to) {
        Point a = StraightPath.attach(from, to.center());
        Point b = StraightPath.attach(to, from.center());
        if (Math.abs(a.x() - b.x()) < 8 || Math.abs(a.y() - b.y()) < 8) {
            return new EdgePath(List.of(a, b));
        }
        Point mid = new Point(a.x(), b.y());
        return new EdgePath(List.of(a, mid, b));
    }
}
