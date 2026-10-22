package jetuml.model;

import jetuml.model.edges.EdgeType;

public final class Edge extends AbstractElement {
    private final EdgeType type;
    private Endpoint start;
    private Endpoint end;

    public Edge(EdgeType type, Endpoint start, Endpoint end) {
        super(type.descriptors());
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public Edge(String id, EdgeType type, Endpoint start, Endpoint end) {
        super(id, type.descriptors());
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public EdgeType type() {
        return type;
    }

    @Override
    public ElementKind kind() {
        return type.kind();
    }

    public ElementKind edgeKind() {
        return type.kind();
    }

    public Endpoint start() {
        return start;
    }

    public Endpoint end() {
        return end;
    }

    public void setStart(Endpoint start) {
        this.start = start;
    }

    public void setEnd(Endpoint end) {
        this.end = end;
    }

    public boolean involves(Node node) {
        return (start instanceof NodeEndpoint ne && ne.node() == node)
                || (end instanceof NodeEndpoint ne2 && ne2.node() == node);
    }
}
