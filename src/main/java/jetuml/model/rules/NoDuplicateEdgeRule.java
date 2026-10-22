package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.Edge;
import jetuml.model.Endpoint;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;

public final class NoDuplicateEdgeRule implements ConnectionRule {
    private final boolean exemptStates;

    public NoDuplicateEdgeRule(boolean exemptStates) {
        this.exemptStates = exemptStates;
    }

    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (exemptStates && type == EdgeType.TRANSITION) {
            return Verdict.accept();
        }
        if (!(from instanceof NodeEndpoint a) || !(to instanceof NodeEndpoint b)) {
            return Verdict.accept();
        }
        for (Edge e : d.edges()) {
            if (e.type() == type
                    && e.start() instanceof NodeEndpoint s
                    && e.end() instanceof NodeEndpoint t
                    && s.node() == a.node()
                    && t.node() == b.node()) {
                return Verdict.reject("An identical edge already exists.");
            }
        }
        return Verdict.accept();
    }
}
