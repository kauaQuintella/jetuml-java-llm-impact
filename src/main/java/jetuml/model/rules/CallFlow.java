package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.Edge;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;
import jetuml.model.nodes.CallNode;
import jetuml.model.nodes.ImplicitParameterNode;

import java.util.Comparator;
import java.util.List;

/**
 * Read-only analysis of calls and returns on a lifeline.
 */
public final class CallFlow {
    private final DiagramView diagram;
    private final ImplicitParameterNode owner;

    public CallFlow(DiagramView diagram, ImplicitParameterNode owner) {
        this.diagram = diagram;
        this.owner = owner;
    }

    public boolean keepsValidControlFlow(CallNode from, CallNode to, EdgeType type) {
        if (from == to) {
            return type == EdgeType.CALL;
        }
        List<CallNode> ordered = owner.children().stream()
                .filter(CallNode.class::isInstance)
                .map(CallNode.class::cast)
                .sorted(Comparator.comparingInt(n -> n.position().y()))
                .toList();
        if (type == EdgeType.RETURN) {
            return nestedInside(from, to) || nestedInside(to, from);
        }
        if (!ordered.contains(from) && from.parent().orElse(null) == owner) {
            return true;
        }
        int fromY = from.position().y();
        int toY = to.position().y();
        if (toY < fromY - 4) {
            return false;
        }
        return !hasCrossingReturn(from, to);
    }

    private boolean nestedInside(CallNode inner, CallNode outer) {
        return inner.position().y() >= outer.position().y()
                && inner.position().y() + inner.height() <= outer.position().y() + outer.height();
    }

    private boolean hasCrossingReturn(CallNode from, CallNode to) {
        for (Edge e : diagram.edges()) {
            if (e.type() != EdgeType.RETURN) {
                continue;
            }
            if (!(e.start() instanceof NodeEndpoint s) || !(e.end() instanceof NodeEndpoint t)) {
                continue;
            }
            Node a = s.node();
            Node b = t.node();
            int y1 = Math.min(a.position().y(), b.position().y());
            int y2 = Math.max(a.position().y(), b.position().y());
            int ny = Math.min(from.position().y(), to.position().y());
            if (ny > y1 && ny < y2) {
                return true;
            }
        }
        return false;
    }
}
