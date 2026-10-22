package jetuml.model;

import jetuml.model.types.DiagramType;

import java.util.List;
import java.util.Optional;

public interface DiagramView {
    DiagramType type();

    List<Node> rootNodes();

    List<Edge> edges();

    List<Edge> edgesOf(Node n);

    Optional<Node> findNode(String id);

    List<Node> allNodes();
}
