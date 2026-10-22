package jetuml.render;

import jetuml.model.Point;

import java.util.List;

public record EdgePath(List<Point> points) {
    public Point start() {
        return points.getFirst();
    }

    public Point end() {
        return points.getLast();
    }

    public Point middle() {
        return points.get(points.size() / 2);
    }
}
