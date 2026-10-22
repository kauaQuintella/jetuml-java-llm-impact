package jetuml.app;

import jetuml.model.Point;

public final class SnapService {
    public static final int GRID = 10;

    private SnapService() {
    }

    public static int snap(int value) {
        return Math.round(value / (float) GRID) * GRID;
    }

    public static Point snap(Point p) {
        return new Point(snap(p.x()), snap(p.y()));
    }
}
