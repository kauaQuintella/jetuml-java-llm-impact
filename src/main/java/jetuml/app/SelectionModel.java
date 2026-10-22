package jetuml.app;

import jetuml.model.Edge;
import jetuml.model.Element;
import jetuml.model.Node;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class SelectionModel {
    private final Set<Node> nodes = new LinkedHashSet<>();
    private final Set<Edge> edges = new LinkedHashSet<>();

    public Set<Node> nodes() {
        return Set.copyOf(nodes);
    }

    public Set<Edge> edges() {
        return Set.copyOf(edges);
    }

    public boolean isEmpty() {
        return nodes.isEmpty() && edges.isEmpty();
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }

    public void set(Node node) {
        clear();
        nodes.add(node);
    }

    public void set(Edge edge) {
        clear();
        edges.add(edge);
    }

    public void toggle(Node node) {
        if (!nodes.remove(node)) {
            nodes.add(node);
        }
    }

    public void add(Node node) {
        nodes.add(node);
    }

    public void add(Edge edge) {
        edges.add(edge);
    }

    public boolean contains(Node node) {
        return nodes.contains(node);
    }

    public boolean contains(Edge edge) {
        return edges.contains(edge);
    }

    public Optional<Element> single() {
        if (nodes.size() == 1 && edges.isEmpty()) {
            return Optional.of(nodes.iterator().next());
        }
        if (edges.size() == 1 && nodes.isEmpty()) {
            return Optional.of(edges.iterator().next());
        }
        return Optional.empty();
    }

    public boolean hasSingleElement() {
        return single().isPresent();
    }

    public void addAll(java.util.Collection<Node> nodeList, java.util.Collection<Edge> edgeList) {
        nodes.addAll(nodeList);
        edges.addAll(edgeList);
    }

    public List<Element> all() {
        java.util.ArrayList<Element> all = new java.util.ArrayList<>();
        all.addAll(nodes);
        all.addAll(edges);
        return all;
    }
}
