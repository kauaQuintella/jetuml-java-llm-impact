package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.Edge;
import jetuml.model.Endpoint;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;

import java.util.Set;

public final class NoAssociationPlusAggregationRule implements ConnectionRule {
    private static final Set<EdgeType> GROUP = Set.of(
            EdgeType.ASSOCIATION, EdgeType.AGGREGATION, EdgeType.COMPOSITION);

    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (!GROUP.contains(type)) {
            return Verdict.accept();
        }
        if (!(from instanceof NodeEndpoint a) || !(to instanceof NodeEndpoint b)) {
            return Verdict.accept();
        }
        for (Edge e : d.edges()) {
            if (!GROUP.contains(e.type())) {
                continue;
            }
            if (!(e.start() instanceof NodeEndpoint s) || !(e.end() instanceof NodeEndpoint t)) {
                continue;
            }
            boolean samePair = (s.node() == a.node() && t.node() == b.node())
                    || (s.node() == b.node() && t.node() == a.node());
            if (samePair && e.type() != type) {
                return Verdict.reject("Association, aggregation and composition cannot mix between the same nodes.");
            }
        }
        return Verdict.accept();
    }
}
