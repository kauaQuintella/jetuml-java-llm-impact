package jetuml.model.command;

import jetuml.model.Node;
import jetuml.model.Vector;

import java.util.LinkedHashSet;
import java.util.Set;

public final class MoveNodesCommand implements Command {
    private final Set<Node> nodes;
    private final Vector delta;

    public MoveNodesCommand(Set<Node> nodes, Vector delta) {
        this.nodes = new LinkedHashSet<>(nodes);
        this.delta = delta;
    }

    @Override
    public void execute() {
        for (Node n : nodes) {
            n.translate(delta);
        }
    }

    @Override
    public void undo() {
        Vector reverse = new Vector(-delta.dx(), -delta.dy());
        for (Node n : nodes) {
            n.translate(reverse);
        }
    }
}
