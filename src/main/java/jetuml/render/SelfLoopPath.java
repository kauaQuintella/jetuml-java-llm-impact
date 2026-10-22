package jetuml.render;

import jetuml.model.Point;
import jetuml.model.Rect;

import java.util.List;

public final class SelfLoopPath implements EdgePathStrategy {
    private final int index;

    public SelfLoopPath() {
        this(0);
    }

    public SelfLoopPath(int index) {
        this.index = index;
    }

    @Override
    public EdgePath path(Rect from, Rect to) {
        int offset = 24 + index * 16;
        Point start = new Point(from.maxX(), from.y() + 10 + index * 12);
        Point p1 = new Point(from.maxX() + offset, start.y());
        Point p2 = new Point(from.maxX() + offset, from.y() + 40 + index * 12);
        Point end = new Point(from.maxX(), from.y() + 40 + index * 12);
        return new EdgePath(List.of(start, p1, p2, end));
    }
}
