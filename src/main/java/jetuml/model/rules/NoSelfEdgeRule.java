package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.ElementKind;
import jetuml.model.Endpoint;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;

import java.util.Set;

public final class NoSelfEdgeRule implements ConnectionRule {
    private final Set<EdgeType> forbiddenTypes;
    private final Set<ElementKind> forbiddenNodes;

    public NoSelfEdgeRule(Set<EdgeType> forbiddenTypes, Set<ElementKind> forbiddenNodes) {
        this.forbiddenTypes = Set.copyOf(forbiddenTypes);
        this.forbiddenNodes = Set.copyOf(forbiddenNodes);
    }

    public static NoSelfEdgeRule forTypes(EdgeType... types) {
        return new NoSelfEdgeRule(Set.of(types), Set.of());
    }

    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (!(from instanceof NodeEndpoint a) || !(to instanceof NodeEndpoint b)) {
            return Verdict.accept();
        }
        if (a.node() != b.node()) {
            return Verdict.accept();
        }
        if (forbiddenTypes.contains(type) || forbiddenNodes.contains(a.node().kind())) {
            return Verdict.reject("A node cannot connect to itself with this edge.");
        }
        return Verdict.accept();
    }
}
