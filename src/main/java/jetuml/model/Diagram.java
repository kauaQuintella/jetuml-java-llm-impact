package jetuml.model;

import jetuml.model.nodes.FieldNode;
import jetuml.model.types.DiagramType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Diagram implements DiagramView {
    private final DiagramType type;
    private final List<Node> roots = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public Diagram(DiagramType type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public DiagramType type() {
        return type;
    }

    @Override
    public List<Node> rootNodes() {
        return List.copyOf(roots);
    }

    @Override
    public List<Edge> edges() {
        return List.copyOf(edges);
    }

    @Override
    public List<Edge> edgesOf(Node n) {
        return edges.stream().filter(e -> e.involves(n)).toList();
    }

    @Override
    public Optional<Node> findNode(String id) {
        return allNodes().stream().filter(n -> n.id().equals(id)).findFirst();
    }

    @Override
    public List<Node> allNodes() {
        List<Node> all = new ArrayList<>();
        for (Node root : roots) {
            collect(root, all);
        }
        return all;
    }

    private void collect(Node node, List<Node> out) {
        out.add(node);
        for (Node child : node.children()) {
            collect(child, out);
        }
    }

    public void addRoot(Node node) {
        if (node instanceof FieldNode) {
            throw new IllegalArgumentException("A field cannot exist as a root node");
        }
        if (node.parent().isPresent()) {
            node.parent().get().removeChild(node);
        }
        if (!roots.contains(node)) {
            roots.add(node);
        }
    }

    public void removeRoot(Node node) {
        roots.remove(node);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    public void removeNode(Node node) {
        node.parent().ifPresentOrElse(p -> p.removeChild(node), () -> roots.remove(node));
        edges.removeIf(e -> e.involves(node) || node.children().stream().anyMatch(e::involves));
        for (Node child : List.copyOf(node.children())) {
            removeNode(child);
        }
    }
}
