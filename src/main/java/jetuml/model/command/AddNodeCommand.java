package jetuml.model.command;

import jetuml.model.Diagram;
import jetuml.model.Node;

import java.util.Optional;

public final class AddNodeCommand implements Command {
    private final Diagram diagram;
    private final Node node;
    private final Optional<Node> parent;

    public AddNodeCommand(Diagram diagram, Node node, Optional<Node> parent) {
        this.diagram = diagram;
        this.node = node;
        this.parent = parent;
    }

    @Override
    public void execute() {
        parent.ifPresentOrElse(p -> p.addChild(node), () -> diagram.addRoot(node));
    }

    @Override
    public void undo() {
        parent.ifPresentOrElse(p -> p.removeChild(node), () -> diagram.removeRoot(node));
    }

    public Node node() {
        return node;
    }
}
