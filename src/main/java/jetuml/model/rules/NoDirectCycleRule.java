package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.Edge;
import jetuml.model.Endpoint;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;

import java.util.Set;

public final class NoDirectCycleRule implements ConnectionRule {
    private final Set<EdgeType> types;

    public NoDirectCycleRule(Set<EdgeType> types) {
        this.types = Set.copyOf(types);
    }

    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (!types.contains(type)) {
            return Verdict.accept();
        }
        if (!(from instanceof NodeEndpoint a) || !(to instanceof NodeEndpoint b) || a.node() == b.node()) {
            return Verdict.accept();
        }
        for (Edge e : d.edges()) {
            if (types.contains(e.type())
                    && e.start() instanceof NodeEndpoint s
                    && e.end() instanceof NodeEndpoint t
                    && s.node() == b.node()
                    && t.node() == a.node()) {
                return Verdict.reject("This would create a direct cycle.");
            }
        }
        return Verdict.accept();
    }

    public static boolean wouldCycle(Node from, Node to, DiagramView d, Set<EdgeType> types) {
        return d.edges().stream().anyMatch(e ->
                types.contains(e.type())
                        && e.start() instanceof NodeEndpoint s
                        && e.end() instanceof NodeEndpoint t
                        && s.node() == to
                        && t.node() == from);
    }
}
