package jetuml.model;

public record Point(int x, int y) {
    public Point translate(int dx, int dy) {
        return new Point(x + dx, y + dy);
    }

    public Point translate(Vector v) {
        return translate(v.dx(), v.dy());
    }

    public Vector minus(Point other) {
        return new Vector(x - other.x, y - other.y);
    }
}
