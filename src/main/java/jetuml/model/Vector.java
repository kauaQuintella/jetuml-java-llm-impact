package jetuml.model;

public record Vector(int dx, int dy) {
    public static final Vector ZERO = new Vector(0, 0);
}
