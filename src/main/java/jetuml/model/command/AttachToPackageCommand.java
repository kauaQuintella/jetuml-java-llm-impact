package jetuml.model.command;

import jetuml.model.Diagram;
import jetuml.model.Node;
import jetuml.model.nodes.PackageNode;

import java.util.Optional;

public final class AttachToPackageCommand implements Command {
    private final Diagram diagram;
    private final Node node;
    private final PackageNode target;
    private final boolean attach;
    private Optional<Node> previousParent = Optional.empty();
    private boolean wasRoot;

    public AttachToPackageCommand(Diagram diagram, Node node, PackageNode target, boolean attach) {
        this.diagram = diagram;
        this.node = node;
        this.target = target;
        this.attach = attach;
    }

    @Override
    public void execute() {
        previousParent = node.parent();
        wasRoot = previousParent.isEmpty() && diagram.rootNodes().contains(node);
        if (attach) {
            if (wasRoot) {
                diagram.removeRoot(node);
            }
            previousParent.ifPresent(p -> p.removeChild(node));
            target.addChild(node);
        } else {
            target.removeChild(node);
            diagram.addRoot(node);
        }
    }

    @Override
    public void undo() {
        if (attach) {
            target.removeChild(node);
            previousParent.ifPresentOrElse(p -> p.addChild(node), () -> diagram.addRoot(node));
        } else {
            diagram.removeRoot(node);
            target.addChild(node);
        }
    }
}
