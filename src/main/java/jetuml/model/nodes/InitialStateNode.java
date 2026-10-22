package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Point;

import java.util.List;

public final class InitialStateNode extends AbstractNode {
    public InitialStateNode(Point position) {
        super(List.of(), position);
    }

    public InitialStateNode(String id, Point position) {
        super(id, List.of(), position);
    }

    @Override
    public ElementKind kind() {
        return ElementKind.INITIAL_STATE_NODE;
    }
}
