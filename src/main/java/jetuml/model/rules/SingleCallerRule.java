package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.Edge;
import jetuml.model.Endpoint;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;

public final class SingleCallerRule implements ConnectionRule {
    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (type != EdgeType.CALL) {
            return Verdict.accept();
        }
        if (!(to instanceof NodeEndpoint callee)) {
            return Verdict.accept();
        }
        Node target = callee.node();
        boolean alreadyCalled = d.edges().stream()
                .anyMatch(e -> e.type() == EdgeType.CALL
                        && e.end() instanceof NodeEndpoint n
                        && n.node() == target);
        if (alreadyCalled) {
            return Verdict.reject("A call node can have only one caller.");
        }
        return Verdict.accept();
    }
}
