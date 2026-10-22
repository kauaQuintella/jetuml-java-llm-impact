package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.Edge;
import jetuml.model.Endpoint;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;

public final class NoCollaborationCycleRule implements ConnectionRule {
    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (type != EdgeType.COLLABORATION) {
            return Verdict.accept();
        }
        if (!(from instanceof NodeEndpoint a) || !(to instanceof NodeEndpoint b)) {
            return Verdict.accept();
        }
        for (Edge e : d.edges()) {
            if (e.type() == EdgeType.COLLABORATION
                    && e.start() instanceof NodeEndpoint s
                    && e.end() instanceof NodeEndpoint t
                    && s.node() == b.node()
                    && t.node() == a.node()) {
                return Verdict.reject("Collaboration edges cannot form a cycle.");
            }
        }
        return Verdict.accept();
    }
}
