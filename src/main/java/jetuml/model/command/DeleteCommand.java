package jetuml.model.command;

import jetuml.model.Diagram;
import jetuml.model.Edge;
import jetuml.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DeleteCommand implements Command {
    private final Diagram diagram;
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final List<Optional<Node>> parents = new ArrayList<>();
    private boolean captured;

    public DeleteCommand(Diagram diagram, List<Node> nodes, List<Edge> edges) {
        this.diagram = diagram;
        this.nodes = List.copyOf(nodes);
        this.edges = List.copyOf(edges);
    }

    @Override
    public void execute() {
        if (!captured) {
            for (Node n : nodes) {
                parents.add(n.parent());
            }
            captured = true;
        }
        for (Edge e : edges) {
            diagram.removeEdge(e);
        }
        for (Node n : nodes) {
            n.parent().ifPresentOrElse(p -> p.removeChild(n), () -> diagram.removeRoot(n));
            diagram.edgesOf(n).forEach(diagram::removeEdge);
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            Optional<Node> parent = parents.get(i);
            parent.ifPresentOrElse(p -> p.addChild(n), () -> diagram.addRoot(n));
        }
        for (Edge e : edges) {
            diagram.addEdge(e);
        }
    }
}
