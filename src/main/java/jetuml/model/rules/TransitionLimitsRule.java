package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.Edge;
import jetuml.model.ElementKind;
import jetuml.model.Endpoint;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;

public final class TransitionLimitsRule implements ConnectionRule {
    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (type != EdgeType.TRANSITION) {
            return Verdict.accept();
        }
        if (!(from instanceof NodeEndpoint a) || !(to instanceof NodeEndpoint b)) {
            return Verdict.reject("A transition must connect two nodes.");
        }
        Node start = a.node();
        Node end = b.node();
        if (start.kind() == ElementKind.FINAL_STATE_NODE) {
            return Verdict.reject("A final state cannot have outgoing transitions.");
        }
        if (end.kind() == ElementKind.INITIAL_STATE_NODE) {
            return Verdict.reject("An initial state cannot have incoming transitions.");
        }
        if (start == end) {
            long self = d.edgesOf(start).stream()
                    .filter(e -> e.type() == EdgeType.TRANSITION && same(e, start, start))
                    .count();
            if (self >= 2) {
                return Verdict.reject("A state can have at most two self-transitions.");
            }
            return Verdict.accept();
        }
        long sameDir = d.edges().stream()
                .filter(e -> e.type() == EdgeType.TRANSITION && same(e, start, end))
                .count();
        if (sameDir >= 2) {
            return Verdict.reject("At most two transitions are allowed in the same direction.");
        }
        return Verdict.accept();
    }

    private static boolean same(Edge e, Node from, Node to) {
        return e.start() instanceof NodeEndpoint s
                && e.end() instanceof NodeEndpoint t
                && s.node() == from
                && t.node() == to;
    }
}
