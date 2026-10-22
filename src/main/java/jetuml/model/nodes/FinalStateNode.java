package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Point;

import java.util.List;

public final class FinalStateNode extends AbstractNode {
    public FinalStateNode(Point position) {
        super(List.of(), position);
    }

    public FinalStateNode(String id, Point position) {
        super(id, List.of(), position);
    }

    @Override
    public ElementKind kind() {
        return ElementKind.FINAL_STATE_NODE;
    }
}
