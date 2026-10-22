package jetuml.model;

public record Rect(int x, int y, int width, int height) {
    public int maxX() {
        return x + width;
    }

    public int maxY() {
        return y + height;
    }

    public Point origin() {
        return new Point(x, y);
    }

    public Point center() {
        return new Point(x + width / 2, y + height / 2);
    }

    public boolean contains(Point p) {
        return p.x() >= x && p.x() <= maxX() && p.y() >= y && p.y() <= maxY();
    }

    public boolean intersects(Rect other) {
        return x < other.maxX() && maxX() > other.x && y < other.maxY() && maxY() > other.y;
    }

    public Rect translated(int dx, int dy) {
        return new Rect(x + dx, y + dy, width, height);
    }

    public Rect union(Rect other) {
        int nx = Math.min(x, other.x);
        int ny = Math.min(y, other.y);
        int mx = Math.max(maxX(), other.maxX());
        int my = Math.max(maxY(), other.maxY());
        return new Rect(nx, ny, mx - nx, my - ny);
    }

    public static Rect fromCorners(Point a, Point b) {
        int nx = Math.min(a.x(), b.x());
        int ny = Math.min(a.y(), b.y());
        return new Rect(nx, ny, Math.abs(a.x() - b.x()), Math.abs(a.y() - b.y()));
    }
}
