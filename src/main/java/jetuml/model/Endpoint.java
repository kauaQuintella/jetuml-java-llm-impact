package jetuml.model;

public sealed interface Endpoint permits NodeEndpoint, PointEndpoint {
    Point position();
}
