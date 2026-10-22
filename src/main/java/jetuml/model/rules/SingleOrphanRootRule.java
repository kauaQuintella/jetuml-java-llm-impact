package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.ElementKind;
import jetuml.model.Endpoint;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;

public final class SingleOrphanRootRule implements ConnectionRule {
    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (type != EdgeType.CALL) {
            return Verdict.accept();
        }
        long orphans = d.allNodes().stream()
                .filter(n -> n.kind() == ElementKind.CALL_NODE)
                .filter(n -> d.edges().stream().noneMatch(e -> e.type() == EdgeType.CALL
                        && e.end() instanceof NodeEndpoint ne
                        && ne.node() == n))
                .count();
        Node start = from instanceof NodeEndpoint ne ? ne.node() : null;
        boolean startIsOrphan = start != null && start.kind() == ElementKind.CALL_NODE
                && d.edges().stream().noneMatch(e -> e.type() == EdgeType.CALL
                && e.end() instanceof NodeEndpoint ne2
                && ne2.node() == start);
        if (orphans > 1 && startIsOrphan) {
            return Verdict.reject("A sequence diagram can have only one root call.");
        }
        return Verdict.accept();
    }
}
