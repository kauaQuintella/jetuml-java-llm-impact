package jetuml.model;

public record NodeEndpoint(Node node) implements Endpoint {
    @Override
    public Point position() {
        return node.position();
    }
}
