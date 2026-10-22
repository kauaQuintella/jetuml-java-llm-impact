package jetuml.model;

public record PointEndpoint(Point point) implements Endpoint {
    @Override
    public Point position() {
        return point;
    }
}
