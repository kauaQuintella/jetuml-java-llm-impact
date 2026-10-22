package jetuml.model.command;

import jetuml.model.Diagram;
import jetuml.model.Edge;

public final class AddEdgeCommand implements Command {
    private final Diagram diagram;
    private final Edge edge;

    public AddEdgeCommand(Diagram diagram, Edge edge) {
        this.diagram = diagram;
        this.edge = edge;
    }

    @Override
    public void execute() {
        diagram.addEdge(edge);
    }

    @Override
    public void undo() {
        diagram.removeEdge(edge);
    }

    public Edge edge() {
        return edge;
    }
}
